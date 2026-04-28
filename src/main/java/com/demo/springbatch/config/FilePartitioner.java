package com.demo.springbatch.config;

import com.demo.springbatch.model.FileMetadata;
import com.demo.springbatch.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilePartitioner implements Partitioner {

    @Value("file:./input/*.csv")
    private Resource[] resources;

    private final FileMetadataRepository repository;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Map<String, ExecutionContext> partitions = new HashMap<>();
        log.info("Number of files found: {}", resources.length);

        int i = 0;

        for (Resource resource : resources) {

            try {
                String filePath = resource.getFile().getAbsolutePath();
                String fileName = resource.getFilename();

                Optional<FileMetadata> existing = repository.findByFileName(fileName);

                // ==== RETRY + FILTER LOGIC START =====
                if (existing.isEmpty()) {
                    FileMetadata meta = new FileMetadata();
                    meta.setFileName(fileName);
                    meta.setStatus("NEW");
                    meta.setRecordCount(0); // IMPORTANT
                    meta.setCreatedAt(LocalDateTime.now());
                    meta.setUpdatedAt(LocalDateTime.now());
                    repository.save(meta);

                    log.info("NEW file added: {}", fileName);

                } else if ("FAILED".equals(existing.get().getStatus())) {

                    // RETRY FAILED FILE
                    log.info("Retrying FAILED file: {}", fileName);

                } else {
                    // SKIP COMPLETED FILE
                    log.info("Skipping already processed file: {}", fileName);
                    continue; // VERY IMPORTANT
                }
                // ===== RETRY + FILTER LOGIC END =====

                ExecutionContext context = new ExecutionContext();
                context.putString("file", filePath); // String (IMPORTANT FIX)
                partitions.put("partition" + i, context);

                log.info("Partition {} created for file: {}", "partition" + i, fileName);
                i++;

            } catch (Exception e) {
                throw new RuntimeException("Error reading file: " + resource.getFilename(), e);
            }
        }
        log.info("Total partitions created: {}", partitions.size());
        return partitions;
    }
}
