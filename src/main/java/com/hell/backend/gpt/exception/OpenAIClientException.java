package com.hell.backend.gpt.exception;


import com.hell.backend.common.exception.CustomException;

public class OpenAIClientException extends CustomException {
    public OpenAIClientException(String message) {
        super(message);
    }

    public OpenAIClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
