package com.demo.springbatch.config;

import com.demo.springbatch.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileStepListener implements StepExecutionListener {

    private final FileMetadataRepository repository;

    @Override
    public void beforeStep(StepExecution stepExecution) {

        String filePath = stepExecution.getExecutionContext().getString("file");
        String fileName = new File(filePath).getName();

        repository.findByFileName(fileName).ifPresent(meta -> {
            meta.setStatus("PROCESSING");
            meta.setStartTime(LocalDateTime.now());
            meta.setUpdatedAt(LocalDateTime.now());
            repository.save(meta);
        });

        log.info("STARTED processing file: {}", fileName);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        String filePath = stepExecution.getExecutionContext().getString("file");
        String fileName = new File(filePath).getName();

        String status = stepExecution.getStatus().toString();

        repository.findByFileName(fileName).ifPresent(meta -> {
            meta.setStatus(status);
            meta.setEndTime(LocalDateTime.now());
            meta.setUpdatedAt(LocalDateTime.now());
            repository.save(meta);
        });

        log.info("Processing file: {} with status: {}",
                fileName, status);

        return stepExecution.getExitStatus();
    }
}
