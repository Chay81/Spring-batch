package com.demo.springbatch.exceptions;

public class NoFileFoundException extends RuntimeException {
    public NoFileFoundException(String message) {
        super(message);
    }
}
