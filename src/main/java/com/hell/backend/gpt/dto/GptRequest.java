package com.hell.backend.gpt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GptRequest {

    @Schema(description = "메시지 배열")
    private List<Message> messages;

    @Getter
    @Setter
    public static class Message {

        @Schema(description = "메시지 역할", example = "user")
        private String role;

        @Schema(description = "메시지 내용")
        private String content;

        @Schema(description = "메시지 일시", example = "2024-11-10T15:18:12+09:00")
        private String datetime;
    }
}
