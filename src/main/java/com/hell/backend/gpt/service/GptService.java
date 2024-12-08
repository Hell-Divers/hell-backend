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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GptService {

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    public GptResponse processChatGptRequest(GptRequest request, Long userId, BigDecimal balance) {
        try {
            // 1. 시스템 프롬프트 설정
            String systemPrompt = String.format(
                GptPrompt.CHAT_PROMPT,
                balance.toString()
            );

            // 2. 메시지 구성
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            
            // 3. 사용자 메시지 추가
            for (GptRequest.Message msg : request.getMessages()) {
                // 메시지 내용을 JSON으로 변환
                Map<String, String> content = new HashMap<>();
                content.put("message", msg.getContent());
                content.put("datetime", msg.getDatetime());
                content.put("userBalance", balance.toString());
                
                messages.add(Map.of(
                    "role", msg.getRole(),
                    "content", objectMapper.writeValueAsString(content)
                ));
            }

            // 4. GPT 호출
            String gptResponse = openAIClient.getChatResponse(messages);
            log.debug("Raw GPT response: {}", gptResponse);

            return parseGptResponse(gptResponse);
            
        } catch (Exception e) {
            log.error("GPT processing error: {}", e.getMessage(), e);
            throw new GptResponseParseException("GPT 처리 중 오류 발생", e);
        }
    }

    private GptResponse parseGptResponse(String gptReply) {
        try {
            // 1. 전체 응답 파싱
            JsonNode rootNode = objectMapper.readTree(gptReply);
            JsonNode choicesNode = rootNode.path("choices");
            
            if (!choicesNode.isArray() || choicesNode.size() == 0) {
                throw new GptResponseParseException("Invalid GPT response: no choices found");
            }

            // 2. content 추출
            JsonNode messageNode = choicesNode.get(0).path("message");
            JsonNode contentNode = messageNode.path("content");
            
            if (contentNode.isMissingNode()) {
                throw new GptResponseParseException("Invalid GPT response: no content found");
            }

            String contentStr = contentNode.asText();
            log.debug("Content to parse: {}", contentStr);

            // 3. content JSON 파싱
            JsonNode contentJson = objectMapper.readTree(contentStr);
            
            // 4. GptResponse 객체 생성
            GptResponse response = new GptResponse();
            response.setMessage(contentJson.path("message").asText());
            response.setState(contentJson.path("state").asText());

            // 5. expenses 처리
            JsonNode expensesNode = contentJson.path("expenses");
            if (!expensesNode.isMissingNode() && expensesNode.isArray()) {
                List<GptResponse.ExpenseData> expenses = new ArrayList<>();
                for (JsonNode expenseNode : expensesNode) {
                    GptResponse.ExpenseData expense = new GptResponse.ExpenseData();
                    expense.setAmount(expenseNode.path("amount").asDouble());
                    expense.setDatetime(expenseNode.path("datetime").asText());
                    expense.setCategory(mapCategory(expenseNode.path("category").asText()));
                    expense.setLocation(expenseNode.path("location").asText());
                    expense.setType(expenseNode.path("type").asText());
                    expenses.add(expense);
                }
                response.setExpenses(expenses);
            }

            return response;

        } catch (Exception e) {
            log.error("Parse error: {}\nRaw reply: {}", e.getMessage(), gptReply);
            throw new GptResponseParseException("Failed to parse GPT response", e);
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

    private String mapCategory(String category) {
        return switch (category.toLowerCase()) {
            case "식비" -> "식사";
            case "교통비" -> "교통";
            case "쇼핑비" -> "쇼핑";
            // ... 기타 매핑
            default -> "기타";
        };
    }
}