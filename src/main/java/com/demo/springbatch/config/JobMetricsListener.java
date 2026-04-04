package com.demo.springbatch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobMetricsListener implements JobExecutionListener {

    private final Set<String> globalEmailSet;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("JOB STARTED: {}", jobExecution.getJobInstance().getJobName());
        // Clear the global email set at the start of each job run
        globalEmailSet.clear();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        log.info("JOB ENDED: {}", jobExecution.getStatus());

        jobExecution.getStepExecutions().forEach(stepExecution -> {
            log.info(
                    "Step: {} | Read: {} | Written: {} | Threads: {}",
                    stepExecution.getStepName(),
                    stepExecution.getReadCount(),
                    stepExecution.getWriteCount(),
                    Thread.currentThread().getName()
            );
        });
    }
}
