package com.hell.backend.gpt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hell.backend.gpt.dto.GptPrompt;
import com.hell.backend.gpt.dto.GptRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * OpenAI API 호출을 담당하는 서비스.
 * GptService에서 비즈니스 로직 및 응답 파싱을 수행하고,
 * OpenAIService는 OpenAIClient를 통해 단순히 GPT API를 호출하고 결과를 반환한다.
 */
@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * system 메시지와 사용자 메시지를 전달받아 OpenAI API를 호출하고 응답을 반환
     * 
     * @param userMessages  사용자 대화 메시지 목록
     * @param balance       현재 잔액 (필요 시 메시지 컨텐츠에 반영)
     * @return GPT의 응답 내용 (JSON 문자열)
     */
    public String sendChatRequest(List<GptRequest.Message> userMessages, BigDecimal balance) {

        try {
            // 시스템 프롬프트 설정
            String systemPrompt = String.format(
                GptPrompt.CHAT_PROMPT,
                balance.toString()
            );

            // GPT 메시지 구성
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));

            // user/assistant 메시지 추가
            for (GptRequest.Message msg : userMessages) {
                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put("message", msg.getContent());
                contentMap.put("datetime", msg.getDatetime());
                contentMap.put("userBalance", balance.toString());

                String contentJson = objectMapper.writeValueAsString(contentMap);
                messages.add(Map.of(
                        "role", msg.getRole(),
                        "content", contentJson
                ));
            }

            // OpenAIClient를 통해 실제 GPT API 호출
            String gptResponse = openAIClient.getChatResponse(messages);

            return gptResponse;
        } catch (Exception e) {
            throw new RuntimeException("OpenAI API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
