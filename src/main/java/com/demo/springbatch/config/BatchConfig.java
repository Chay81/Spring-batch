package com.demo.springbatch.config;

import com.demo.springbatch.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.nio.file.Paths;

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
    public Step loadCsvStep(JobRepository jobRepository,
                            PlatformTransactionManager txManager,
                            FlatFileItemReader<User> reader,
                            DuplicateSkippingProcessor processor,
                            ItemWriter<User> metricsItemWriter) {

        return new StepBuilder("loadCsvStep", jobRepository)
//                .<User, User>chunk(1000, txManager)// for testing with 100 records
                .<User, User>chunk(10000, txManager)// for testing with 1 million records
                .reader(reader)      // CSV used here
                .processor(processor)
                .writer(metricsItemWriter)     // insert into DB
                .faultTolerant()
                .skipLimit(100000)
                .skip(DataIntegrityViolationException.class)
                .build();
    }

    @Bean
    @StepScope
    public MultiResourcePartitioner partitioner(
            @Value("#{jobParameters['fileName']}") String fileName,
            @Value("${input.dir}") String inputDir) throws IOException {

        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();

        String fullPath = Paths.get(inputDir, fileName).toString();

        Resource resource = new FileSystemResource(fullPath);

        log.info("Processing file: {}", fullPath);
        log.info("Number of files found: {}", fileName);

        partitioner.setResources(new Resource[]{resource});
        partitioner.setKeyName("fileName");

        return partitioner;
    }

    @Bean
    public Step masterStep(JobRepository jobRepository,
                           Step loadCsvStep,
                           MultiResourcePartitioner partitioner,
                           TaskExecutor taskExecutorService) {
//        int cores = Runtime.getRuntime().availableProcessors(); // dynamically set grid size

        return new StepBuilder("master-step", jobRepository)
                .partitioner("loadCsvStep", partitioner)
                .step(loadCsvStep)
//                .gridSize(cores)  // use number of CPU cores
                .gridSize(10) // number of parallel threads
                .taskExecutor(taskExecutorService)
                .build();
    }

}
