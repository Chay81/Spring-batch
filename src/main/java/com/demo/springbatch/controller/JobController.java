package com.demo.springbatch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobLauncher jobLauncher;
    private final Job importUserJob;

    @PostMapping("/run")
    public ResponseEntity<String> runJob(@RequestParam String fileName) throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("fileName", fileName)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(importUserJob, params);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Job completed for file: " + fileName);
    }
}
