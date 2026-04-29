package com.demo.springbatch.model;

import java.util.List;

public record ApiResponse(String message, List<String> failedFiles) {
}
