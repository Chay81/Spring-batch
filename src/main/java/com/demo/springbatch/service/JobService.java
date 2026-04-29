package com.demo.springbatch.service;

import com.demo.springbatch.exceptions.NoFileFoundException;
import com.demo.springbatch.model.ApiResponse;
import com.demo.springbatch.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobLauncher jobLauncher;
    private final Job importUserJob;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${app.input-dir}")
    private String inputDir;

    public ApiResponse runJob() throws Exception {

        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("file:" + inputDir + "/users_*.csv");

        if (resources.length == 0) {
            throw new NoFileFoundException("No files found in input directory");
        }

        List<String> currentRunFiles = Arrays.stream(resources)
                .map(Resource::getFilename)
                .toList();

        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(importUserJob, params);

        // Step 3: Detect failures from JobExecution (NOT DB)
        List<String> failedFiles = execution.getStepExecutions()
                .stream()
                .filter(stepExecution -> stepExecution.getStatus() == BatchStatus.FAILED)
                .map(stepExecution -> {
                    try {
                        String filePath = stepExecution
                                .getExecutionContext()
                                .getString("file");

                        return filePath != null
                                ? new File(filePath).getName()
                                : null;

                    } catch (Exception e) {
                        log.error("Error extracting file name from execution context", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        // Step 4: Build response
        if (!failedFiles.isEmpty()) {
            log.warn("Job finished with failures: {}", failedFiles);
            return new ApiResponse("Partial success", failedFiles);
        }

        log.info("Job completed successfully");
        return new ApiResponse("Job Completed Successfully. No failed files.", null);   // cleaner than []
    }
}
