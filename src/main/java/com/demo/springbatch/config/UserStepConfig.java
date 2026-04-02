package com.demo.springbatch.config;

import com.demo.springbatch.model.User;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class UserStepConfig {

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);     // number of threads
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("batch-thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Step userStep(JobRepository jobRepository,
                         PlatformTransactionManager txManager,
                         ItemReader<User> reader,
                         DuplicateSkippingProcessor processor,
                         ItemWriter<User> loggingWriter,
                         ItemWriter<User> metricsWriter) {

        return new StepBuilder("user-step", jobRepository)
                .<User, User>chunk(1000, txManager)// increased chunk size for better performance, later we can adjust based on testing
                .reader(reader)
                .processor(processor) // use DuplicateSkippingProcessor
                .writer(loggingWriter) // loggingWriter already prints thread + chunk size
                .writer(metricsWriter) // metricsWriter wraps loggingWriter and records metrics
                .faultTolerant()
                .skipLimit(10000)
                .skip(DataIntegrityViolationException.class) // specific exception
                .taskExecutor(taskExecutor()) // enable multi-threading
                .build();
    }

}