package com.hell.backend.chat.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestGptService {

    public Map<String, Object> processMessages(List<Map<String, Object>> messages) {
        // 더미 응답 메시지 생성
        Map<String, Object> assistantMessage = new HashMap<>();
        assistantMessage.put("role", "assistant");
        assistantMessage.put("content", "답변을 위한 더미 데이터입니다.");
        assistantMessage.put("state", "accept");

        // 기존 메시지 리스트에 더미 응답 추가
        messages.add(assistantMessage);

        // 전체 응답 구성
        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);

        return response;
    }
} 