package com.hell.backend.gpt.exception;

public class QuotaExceededException extends OpenAIClientException {
    public QuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}

