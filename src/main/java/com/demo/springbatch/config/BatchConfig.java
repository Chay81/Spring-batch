package com.demo.springbatch.config;

import com.demo.springbatch.model.User;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
//@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public Step workerStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           ItemReader<User> reader,
                           ItemProcessor<User, User> processor,
                           JdbcBatchItemWriter<User> writer) {

        return new StepBuilder("worker-step", jobRepository)
                .<User, User>chunk(1000, transactionManager) // NEW STYLE
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Step masterStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           Step workerStep,
                           FilePartitioner partitioner,
                           TaskExecutor taskExecutor) {
//        int cores = Runtime.getRuntime().availableProcessors(); // dynamically set grid size

        return new StepBuilder("master-step", jobRepository)
                .partitioner("worker-step", partitioner)
                .step(workerStep)
//                .gridSize(cores)  // use number of CPU cores
                .gridSize(4) // number of parallel threads
                .taskExecutor(taskExecutor)
                .build();
    }

}
