package com.demo.springbatch.config;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;

public class FailureControlListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext context = stepExecution.getExecutionContext();

        if (!context.containsKey("hasFailed")) {
            context.put("hasFailed", false);
        }
    }
}
