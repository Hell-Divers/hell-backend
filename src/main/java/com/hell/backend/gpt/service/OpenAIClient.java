package com.hell.backend.gpt.service;

import com.hell.backend.gpt.exception.OpenAIClientException;
import com.hell.backend.gpt.exception.QuotaExceededException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String apiKey;

    public String getChatResponse(List<Map<String, String>> messages) {
        System.out.println("=== OpenAIClient.getChatResponse ===");
        System.out.println("API Key exists: " + (apiKey != null));
        System.out.println("Messages count: " + messages.size());
        
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
            System.out.println("Request body: " + objectMapper.writeValueAsString(requestBody));
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            System.out.println("Response status: " + response.getStatusCode());
            Map<String, Object> responseBody = response.getBody();
            // GPT 응답 로깅
            System.out.println("GPT Response Body: " + responseBody);

            // Return full response body as JSON string
            return objectMapper.writeValueAsString(responseBody);

        } catch (JsonProcessingException e) {
            throw new OpenAIClientException("Failed to process JSON", e);
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
}
