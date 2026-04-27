package com.demo.springbatch.config;

import com.demo.springbatch.model.FileMetadata;
import com.demo.springbatch.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileMetadataListener implements StepExecutionListener {

    private final FileMetadataRepository repository;

    @Override
    public void beforeStep(StepExecution stepExecution) {

        String filePath = stepExecution
                .getExecutionContext()
                .getString("file");

        if (filePath != null) {
            String fileName = new File(filePath).getName();

            repository.findByFileName(fileName).ifPresent(meta -> {
                meta.setStartTime(LocalDateTime.now());
                meta.setStatus("IN_PROGRESS");
                repository.save(meta);
            });
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        String filePath = stepExecution
                .getExecutionContext()
                .getString("file");

        if (filePath != null) {
            String fileName = new File(filePath).getName();

            repository.findByFileName(fileName).ifPresent(meta -> {
                meta.setEndTime(LocalDateTime.now());

                if (stepExecution.getStatus().isUnsuccessful()) {
                    meta.setStatus("FAILED");
                } else {
                    meta.setStatus("COMPLETED");
                }

                repository.save(meta);
            });
        }

        return stepExecution.getExitStatus();
    }
}