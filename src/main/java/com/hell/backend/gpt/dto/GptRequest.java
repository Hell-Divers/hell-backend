package com.hell.backend.gpt.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GptRequest {
    private String model;
    private List<Message> messages;

    @Getter
    @Setter
    public static class Message {
        private String role;
        private String content;
    }
}