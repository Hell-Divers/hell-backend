package com.hell.backend.gpt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hell.backend.gpt.exception.OpenAIClientException;
import com.hell.backend.gpt.exception.QuotaExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class OpenAIClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${OPENAI_API_KEY}")
    private String apiKey;

    public String getChatResponse(List<Map<String, Object>> messages) {
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
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            System.out.println("GPT Response Body: " + responseBody);
            return parseResponse(responseBody);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new QuotaExceededException("OpenAI API quota exceeded.", e);
            } else {
                throw new OpenAIClientException("Error occurred while calling OpenAI API.", e);
            }
        } catch (Exception e) {
            throw new OpenAIClientException("Unexpected error occurred while calling OpenAI API.", e);
        }
    }

    private String parseResponse(Map<String, Object> responseBody) {
        if (responseBody != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                if (message != null) {
                    String content = (String) message.get("content");
                    System.out.println("GPT Response Content: " + content);
                    return content;
                }
            }
        }
        return "No response from GPT.";
    }
}
