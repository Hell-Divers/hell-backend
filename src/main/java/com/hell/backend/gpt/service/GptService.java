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
            // GPT 메시지 구성
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 최초 채팅인 경우에만 시스템 프롬프트 추가 (메시지가 1개일 때)
            if (request.getMessages().size() == 1) {
                String systemPrompt = String.format(
                    GptPrompt.CHAT_PROMPT,
                    balance.toString()
                );
                messages.add(Map.of("role", "system", "content", systemPrompt));
            }
            
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

            if (!isJsonValid(gptReply)) {
                throw new GptResponseParseException("GPT 응답이 JSON 형식이 아닙니다: " + gptReply);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(gptReply);

            JsonNode roleNode = rootNode.path("role");
            JsonNode contentNode = rootNode.path("content");

            if (roleNode.isMissingNode() || contentNode.isMissingNode()) {
                throw new GptResponseParseException("응답에서 'role' 또는 'content' 필드가 누락되었습니다.");
            }

            return mapper.treeToValue(rootNode, GptResponse.class);
        } catch (JsonProcessingException e) {
            System.err.println("GPT 응답 파싱 오류: " + gptReply);
            throw new GptResponseParseException("GPT 응답을 파싱하는 중 오류가 발생했습니다.", e);
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