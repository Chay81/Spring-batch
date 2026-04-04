//package com.demo.springbatch.config;

/*
@Configuration
@Deprecated
*/
/*
Note: This class is deprecated as it is class for single thread processing,
* we have moved to the next step for partitioning the files from users.csv
* *//*

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

}*/
