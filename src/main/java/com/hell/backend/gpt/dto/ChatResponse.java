package com.hell.backend.gpt.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatResponse {
    private String message;

    public static ChatResponse error(String message) {
        return new ChatResponse(message);
    }
}