package com.hell.backend.gpt.service;

import com.hell.backend.gpt.exception.OpenAIClientException;
import com.hell.backend.gpt.exception.QuotaExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAIClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openai.api.key}")
    private String apiKey;

    public String getChatResponse(List<Map<String, String>> messages) {
        // API 키 로깅 (디버깅용, 실제 운영에서는 제거)
        System.out.println("Using API Key: " + (apiKey != null ? "Key exists" : "Key is null"));
        
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", messages
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST,
                request, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> responseBody = response.getBody();
            // GPT 응답을 로그로 출력
            System.out.println("GPT Response Body: " + responseBody);
            return parseResponse(responseBody);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                // 할당량 초과 예외 처리
                throw new QuotaExceededException("OpenAI API quota exceeded.", e);
            } else {
                // 기타 클라이언트 오류 처리
                throw new OpenAIClientException("Error occurred while calling OpenAI API.", e);
            }
        } catch (Exception e) {
            // 기타 예외 처리
            throw new OpenAIClientException("Unexpected error occurred while calling OpenAI API.", e);
        }
    }

    private String parseResponse(Map<String, Object> responseBody) {
        if (responseBody != null) {
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                if (message != null) {
                    String content = (String) message.get("content");
                    // GPT 응답을 로그로 출력
                    System.out.println("GPT Response Content: " + content);
                    return content;
                }
            }
        }
        return "No response from GPT.";
    }
}
