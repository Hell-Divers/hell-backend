package com.hell.backend.gpt.exception;

import com.hell.backend.common.exception.CustomException;

public class GptResponseParseException extends CustomException {
    public GptResponseParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public GptResponseParseException(String message, String details){
        super(message + ": " + details);
    }
}
