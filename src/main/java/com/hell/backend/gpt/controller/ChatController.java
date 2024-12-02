package com.hell.backend.gpt.controller;

import com.hell.backend.common.security.CustomUserDetails;
import com.hell.backend.gpt.dto.GptRequest;
import com.hell.backend.gpt.dto.GptResponse;
import com.hell.backend.gpt.service.GptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Controller", description = "채팅 관련 API")
public class ChatController {

    private final GptService gptService;

    @Operation(summary = "채팅 메시지 전송", description = "사용자의 채팅 메시지를 처리하고 GPT의 응답을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 처리되었습니다.")
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    @ApiResponse(responseCode = "401", description = "인증 실패입니다.")
    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody GptRequest request, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증이 필요합니다.");
        }
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        GptResponse response = gptService.processChatGptRequest(request, userId);
        return ResponseEntity.ok(response);
    }
}
