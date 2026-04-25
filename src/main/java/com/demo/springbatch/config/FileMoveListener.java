package com.demo.springbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {

        Resource resource = getResource(stepExecution);

        if (resource == null) {
            log.warn("No valid resource found in execution context");
            return ExitStatus.COMPLETED;
        }

        try {
            Path source = resource.getFile().toPath();
            Path target = Path.of(archiveDir, source.getFileName().toString());

            Files.createDirectories(target.getParent());

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            log.info("Moved file: {} → {}", source, target);

        } catch (Exception ex) {
            log.error("Failed to move file: {}", resource.getFilename(), ex);
        }

        return ExitStatus.COMPLETED;
    }

    private Resource getResource(StepExecution stepExecution) {
        Object obj = stepExecution.getExecutionContext().get("file");

        if (obj == null) {
            obj = stepExecution.getJobExecution()
                    .getExecutionContext()
                    .get("file");
        }

        return (obj instanceof Resource) ? (Resource) obj : null;
    }
}