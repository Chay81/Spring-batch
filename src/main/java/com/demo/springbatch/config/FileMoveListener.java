package com.demo.springbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FileMoveListener implements StepExecutionListener {

    @Value("${app.archive-dir}")
    private String archiveDir;

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {

        Resource resource = getResource(stepExecution);

        if (resource != null) {
            // LOGGING (this is your proof of partitioning)
            log.info("Thread: {} Processing File Range: {}",
                    Thread.currentThread().getName(),
                    resource.getFilename());
        }
    }


    private Resource getResource(StepExecution stepExecution) {
        Object obj = stepExecution.getExecutionContext().get("file");

        if (obj == null) {
            obj = stepExecution.getJobExecution()
                    .getExecutionContext()
                    .get("file");
        }

        // THIS IS THE FIX
        if (obj instanceof String path) {
            return new FileSystemResource(path.replace("file:", ""));
        }

        if (obj instanceof Resource resource) {
            return resource;
        }

        return null;
    }
}