package com.demo.springbatch.config;

import com.demo.springbatch.model.FileMetadata;
import com.demo.springbatch.model.User;
import com.demo.springbatch.repository.FileMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.transform.IncorrectTokenCountException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@Slf4j
public class BatchConfig {

    @Bean
    public TaskExecutor taskExecutorService() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);     // number of threads
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("batch-thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public SkipListener<User, User> skipListener() {
        return new SkipListener<>() {
            @Override
            public void onSkipInWrite(@NonNull User item,@NonNull Throwable t) {
                System.out.println("Skipped record: " + item + " | Reason: " + t.getMessage());
            }
        };
    }


    @Bean
    public Step loadCsvStep(JobRepository jobRepository,
                            PlatformTransactionManager txManager,
                            FlatFileItemReader<User> reader,
                            DuplicateSkippingProcessor processor,
                            ItemWriter<User> metricsItemWriter,
                            SkipListener <User, User> skipListener,
                            FileMetadataListener fileMetadataListener,
                            FileStepListener fileStepListener) {

        return new StepBuilder("loadCsvStep", jobRepository)
                .<User, User>chunk(10000, txManager)// for testing with 1 million records
                .reader(reader)      // CSV used here
                .processor(processor)
                .writer(metricsItemWriter)     // insert into DB
                .listener(skipListener)   // move file after processing
                .listener(fileMetadataListener) // update file metadata
                .listener(fileStepListener) // update file log status
                .faultTolerant()
                .skip(DataIntegrityViolationException.class)
                .skip(FlatFileParseException.class)
                .skip(IncorrectTokenCountException.class)
                .skipLimit(100000)
                .build();
    }

    @Bean
    @StepScope
    public MultiResourcePartitioner partitioner(
            @Value("${app.input-dir}") String inputDir,
            FileMetadataRepository repository) throws IOException {

        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();

        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("file:" + inputDir + "/users_*.csv");

        log.info("Number of files found: {}", resources.length);
        List<Resource> filtered = new ArrayList<>();

        for (Resource resource : resources) {
            String fileName = resource.getFilename();

            Optional<FileMetadata> existing = repository.findByFileName(fileName);

            if (existing.isEmpty()) {
                // NEW file
                FileMetadata meta = new FileMetadata();
                meta.setFileName(fileName);
                meta.setStatus("NEW");
                meta.setCreatedAt(LocalDateTime.now());
                meta.setUpdatedAt(LocalDateTime.now());
                repository.save(meta);

                filtered.add(resource);

                log.info("NEW file added: {}", fileName);

            } else if ("FAILED".equals(existing.get().getStatus())) {
                // retry failed
                filtered.add(resource);

                log.info("Retrying FAILED file: {}", fileName);

            } else {
                log.info("Skipping already processed file: {}", fileName);
            }
        }

        log.info("Files selected for processing: {}", filtered.size());


        partitioner.setResources(filtered.toArray(new Resource[0])); // IMPORTANT FIX
        partitioner.setKeyName("file");

        return partitioner;
    }


    @Bean
    public Step masterStep(JobRepository jobRepository,
                           Step loadCsvStep,
                           FilePartitioner partitioner,
                           TaskExecutor taskExecutorService) {

        return new StepBuilder("master-step", jobRepository)
                .partitioner("loadCsvStep", partitioner)
                .step(loadCsvStep)
                .gridSize(10) // number of parallel threads
                .taskExecutor(taskExecutorService)
                .build();
    }

    @Bean
    public Step moveFilesStep(JobRepository jobRepository,
                              PlatformTransactionManager txManager,
                              FileMoveTasklet tasklet) {

        return new StepBuilder("moveFilesStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

}
