package com.hell.backend.chat.controller;

import com.hell.backend.chat.service.TestGptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final TestGptService testGptService;

    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> handleMessage(@RequestBody Map<String, Object> requestBody) {
        // 요청 메시지 추출
        List<Map<String, Object>> messages = (List<Map<String, Object>>) requestBody.get("messages");
        
        // TestGptService를 통해 응답 생성
        Map<String, Object> response = testGptService.processMessages(messages);
        
        return ResponseEntity.ok(response);
    }
}