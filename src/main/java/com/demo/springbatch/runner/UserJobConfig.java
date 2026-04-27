package com.demo.springbatch.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UserJobConfig {

    private final JobRepository jobRepository;
    private final Step masterStep;
    private final Step moveFilesStep;


    @Bean
    public Job importUserJob() {
        return new JobBuilder("importUserJob", jobRepository)
                .start(masterStep)
                .next(moveFilesStep)   // THEN move files
                .build();
    }
}