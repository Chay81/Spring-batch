package com.demo.springbatch.runner;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class JobRunner {

    private final JobLauncher jobLauncher;
    private final Job job;

    @Value("${input.file.path}")
    private String filePath;

    public JobRunner(JobLauncher jobLauncher, Job job) {
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runJob() throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("filePath", filePath)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(job, params);
    }
}