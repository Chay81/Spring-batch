package com.demo.springbatch.exceptions;

import com.demo.springbatch.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NoFileFoundException.class)
    public ResponseEntity<ApiResponse> handleNoFile(NoFileFoundException ex) {

        return ResponseEntity
                .badRequest()
                .body(new ApiResponse(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneric(Exception ex) {

        log.error("Unexpected error", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(ex.getMessage(), List.of()));
    }
}
