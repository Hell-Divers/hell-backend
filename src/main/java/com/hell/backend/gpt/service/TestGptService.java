package com.hell.backend.gpt.service;

import com.hell.backend.gpt.dto.GptRequest;
import com.hell.backend.gpt.dto.GptResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TestGptService {

    public GptResponse processChatGptRequest(GptRequest request, Long userId, BigDecimal currentBalance) {
        GptResponse response = new GptResponse();
        response.setMessage("답변을 위한 더미 데이터입니다.");
        response.setState("accept");
        return response;
    }
} 