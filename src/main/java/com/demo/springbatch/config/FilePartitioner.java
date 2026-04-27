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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                // CREATE METADATA ENTRY
                if (repository.findByFileName(fileName).isEmpty()) {
                    FileMetadata meta = new FileMetadata();
                    meta.setFileName(fileName);
                    meta.setStatus("NEW");
                    meta.setRecordCount(0); // IMPORTANT
                    meta.setCreatedAt(LocalDateTime.now());
                    meta.setUpdatedAt(LocalDateTime.now());
                    repository.save(meta);

                }

                ExecutionContext context = new ExecutionContext();

                context.putString("file", filePath); // String (IMPORTANT FIX)

                partitions.put("partition" + i, context);

                i++;

                log.info("NEW file added: {}", fileName);

            } catch (Exception e) {
                throw new RuntimeException("Error reading file", e);
            }
        }

        return partitions;
    }
}
