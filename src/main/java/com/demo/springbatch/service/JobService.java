package com.demo.springbatch.service;

import com.demo.springbatch.exceptions.NoFileFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobLauncher jobLauncher;
    private final Job importUserJob;

    @Value("${app.input-dir}")
    private String inputDir;

    public void runJob() throws Exception {

        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("file:" + inputDir + "/users_*.csv");

        if (resources.length == 0) {
            throw new NoFileFoundException("No files found in input directory");
        }

        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(importUserJob, params);
    }
}
