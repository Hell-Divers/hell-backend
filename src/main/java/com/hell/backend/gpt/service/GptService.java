package com.hell.backend.gpt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hell.backend.gpt.dto.GptRequest;
import com.hell.backend.gpt.dto.GptResponse;
import com.hell.backend.gpt.dto.GptPrompt;
import com.hell.backend.gpt.exception.GptResponseParseException;
import com.hell.backend.chat.exception.ChatProcessingException;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GptService {

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GptResponse processChatGptRequest(GptRequest request, Long userId, BigDecimal balance) {
        try {
            // 시스템 프롬프트 설정
            String systemPrompt = String.format(
                GptPrompt.CHAT_PROMPT,
                balance.toString()
            );

            // GPT 메시지 구성
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            
            // 사용자 메시지 추가
            for (GptRequest.Message msg : request.getMessages()) {
                Map<String, Object> content = new HashMap<>();
                content.put("message", msg.getContent());
                content.put("datetime", msg.getDatetime());
                content.put("userBalance", balance.toString());
                
                messages.add(Map.of(
                    "role", msg.getRole(),
                    "content", objectMapper.writeValueAsString(content)
                ));
            }

            // GPT API 호출
            String gptResponse = openAIClient.getChatResponse(messages);
            
            // 응답 파싱 및 반환
            return parseGptResponse(gptResponse);
            
        } catch (Exception e) {
            throw new ChatProcessingException("GPT 처리 중 오류 발생: " + e.getMessage());
        }
    }

    private GptResponse parseGptResponse(String gptReply) {
        try {
            JsonNode rootNode = objectMapper.readTree(gptReply);
            JsonNode choicesNode = rootNode.path("choices");
            
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode messageNode = choicesNode.get(0).path("message");
                JsonNode contentNode = messageNode.path("content");
                
                // 디버깅 로그 추가
                String contentStr = contentNode.asText();
                System.out.println("Content to parse: " + contentStr);
                
                try {
                    // 문자열을 JsonNode로 다시 파싱
                    JsonNode contentJson = objectMapper.readTree(contentStr);
                    
                    // JsonNode를 GptResponse 객체로 변환
                    GptResponse response = objectMapper.treeToValue(contentJson, GptResponse.class);
                    
                    // 디버깅 로그 추가
                    System.out.println("Parsed response: " + objectMapper.writeValueAsString(response));
                    
                    return response;
                } catch (Exception e) {
                    System.err.println("Failed to parse content: " + e.getMessage());
                    e.printStackTrace();
                    throw new GptResponseParseException("Failed to parse GPT content: " + e.getMessage());
                }
            }
            
            throw new GptResponseParseException("Invalid GPT response format");
        } catch (JsonProcessingException e) {
            System.err.println("Failed to parse GPT response: " + e.getMessage());
            e.printStackTrace();
            throw new GptResponseParseException("Failed to parse GPT response: " + e.getMessage());
        }
    }

    private boolean isJsonValid(String response) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(response);
            return true;
        } catch (IOException e) {
            return false;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}