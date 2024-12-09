package com.hell.backend.gpt.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class GptResponseParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * OpenAI API 전체 응답(JSON)을 받아, choices[0].message.content를 추출하고,
     * 그 content 안의 JSON을 다시 파싱하여 { "content", "state" } 두 필드만 추출한 뒤 JSON 문자열로 반환.
     */
    public static String extractContentAndState(String openAiResponseJson) {
        try {
            // 1. OpenAI 응답 전체 파싱
            JsonNode root = objectMapper.readTree(openAiResponseJson);

            // 2. choices 배열에서 첫 번째 choice 추출
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.size() == 0) {
                throw new IllegalStateException("OpenAI 응답에서 choices가 비어있습니다.");
            }

            JsonNode firstChoice = choices.get(0);
            JsonNode messageNode = firstChoice.path("message");
            JsonNode contentNode = messageNode.path("content");

            if (contentNode.isMissingNode()) {
                throw new IllegalStateException("OpenAI 응답에 content 필드가 없습니다.");
            }

            // 3. contentNode는 JSON 문자열을 담고 있는 문자열이다.
            //    예: "{\"message\":\"라면이 3500원이었군요!\",\"state\":\"accept\",\"expenses\":[ ... ]}"
            String contentStr = contentNode.asText();

            // 4. contentStr를 JSON으로 다시 파싱
            JsonNode contentJson = objectMapper.readTree(contentStr);

            // 5. contentJson에서 "message"와 "state" 필드 추출
            String message = contentJson.path("message").asText();
            String state = contentJson.path("state").asText();

            // 6. 반환할 JSON 구조 생성
            //    { "content": "라면이 3500원이었군요!", "state": "accept" }
            ObjectNode result = objectMapper.createObjectNode();
            result.put("content", message);
            result.put("state", state);

            // expenses 등의 나머지 데이터는 여기서 사용하지 않고 무시
            // 실제로 필요하다면 여기서 별도로 파싱하거나 저장 로직 수행 가능.

            return objectMapper.writeValueAsString(result);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("응답 파싱 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
