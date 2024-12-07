package com.hell.backend.gpt.exception;

public class GptDataProcessingException extends RuntimeException {
    public GptDataProcessingException(String message) {
        super(message);
    }

    public GptDataProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
} 