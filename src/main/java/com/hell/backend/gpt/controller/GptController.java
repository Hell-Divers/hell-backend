package com.hell.backend.gpt.controller;

import com.hell.backend.gpt.dto.GptRequest;
import com.hell.backend.gpt.dto.GptResponse;
import com.hell.backend.gpt.service.GptService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatgpt")
@RequiredArgsConstructor
public class ChatGptController {

    private final GptService gptService;

    @PostMapping
    public ResponseEntity<GptResponse> getChatGptResponse(@RequestBody GptRequest request, Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        String reply = gptService.processChatGptRequest(request, userId);

        GptResponse response = new GptResponse();
        response.setReply(reply);

        return ResponseEntity.ok(response);
    }
}
