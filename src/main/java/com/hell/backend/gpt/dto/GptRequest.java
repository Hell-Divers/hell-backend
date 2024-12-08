package com.hell.backend.gpt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GptRequest {
    private List<Message> messages;

    @Getter
    @Setter
    public static class Message {
        private String role;
        private String content;
        private String datetime;
    }
}
