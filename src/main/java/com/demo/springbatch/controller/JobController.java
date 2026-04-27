package com.demo.springbatch.controller;

import com.demo.springbatch.model.ApiResponse;
import com.demo.springbatch.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping("/run")
    public ResponseEntity<String> runJob() throws Exception {

        jobService.runJob();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Job Completed successfully");
    }
}
