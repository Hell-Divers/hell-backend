package com.hell.backend.gpt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hell.backend.expense.service.ExpenseService;
import com.hell.backend.gpt.dto.GptRequest;
import com.hell.backend.gpt.dto.GptRequest.Message;
import com.hell.backend.gpt.dto.GptResponse;
import com.hell.backend.gpt.dto.GptResponse.Content;
import com.hell.backend.gpt.entity.GptConversation;
import com.hell.backend.gpt.exception.GptResponseParseException;
import com.hell.backend.gpt.exception.OpenAIClientException;
import com.hell.backend.gpt.repository.GptConversationRepository;
import com.hell.backend.users.entity.User;
import com.hell.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GptService {

    private final OpenAIClient openAIClient;
    private final GptConversationRepository gptConversationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExpenseService expenseService;

    public GptResponse processChatGptRequest(GptRequest request, Long userId) {

        // OpenAI API 호출을 위한 메시지 생성
        List<Map<String, Object>> messages = createMessages(request, userId);

        // GPT로부터 응답 받기
        String gptReply;
        try {
            gptReply = openAIClient.getChatResponse(messages);
            System.out.println("GPT Reply: " + gptReply);
        } catch (OpenAIClientException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답을 받는 중 오류가 발생했습니다.", e);
        }

        // GPT 응답 파싱
        GptResponse response;
        try {
            response = parseGptResponse(gptReply);
        } catch (GptResponseParseException e) {
            response = new GptResponse();
            response.setRole("assistant");
            Content content = new Content();
            content.setMessage("죄송합니다, 응답을 처리하는 중 오류가 발생했습니다.");
            content.setState("error");
            response.setContent(content);
            System.err.println("GPT 응답 파싱 오류: " + e.getMessage());
        }

        // 대화 내역 저장
        saveConversation(request, gptReply, userId);

        // 데이터 저장
        if ("accept".equals(response.getContent().getState()) && response.getContent().getValues() != null) {
            expenseService.addExpenseFromGptData(response, userId);
        }

        return response;
    }

    private List<Map<String, Object>> createMessages(GptRequest request, Long userId) {
        List<Message> userMessages = request.getMessages();

        // 사용자 잔고 계산 (예시로 184000을 사용)
        int balance = 184000;

        // 시스템 메시지 생성
        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", """
                다음 대화를 분석하고 데이터를 구조화하세요. 또한 친근하고 유쾌한 말투로 답변해 주세요.

                응답 형식 예시:
                {
                  "message": "유쾌한 답변 내용",
                  "values": [
                    {
                      "amount": 금액,
                      "datetime": "YYYY-MM-DDTHH:MM:SSZ",
                      "category": "카테고리",
                      "place": "장소",
                      "type": "expense"
                    }
                  ],
                  "state": "accept"
                }

                단, 필요한 정보가 부족할 경우 state를 "request"로 설정하고, message에 추가 정보를 요청하는 내용을 포함해 주세요.
                """
        );

        // 사용자 메시지 생성
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(systemMessage);

        for (Message msg : userMessages) {
            Map<String, Object> userContent = Map.of(
                    "message", msg.getContent(),
                    "userPreference", Map.of(
                            "datetime", msg.getDatetime(),
                            "balance", balance
                    )
            );

            messages.add(Map.of(
                    "role", msg.getRole(),
                    "content", userContent
            ));
        }

        return messages;
    }

    private void saveConversation(GptRequest request, String gptReply, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        GptConversation conversation = new GptConversation();
        conversation.setUser(user);
        conversation.setUserMessage(request.getMessages().toString());
        conversation.setGptResponse(gptReply);
        gptConversationRepository.save(conversation);
    }

    private GptResponse parseGptResponse(String gptReply) {
        try {
            // GPT 응답에서 JSON 부분만 추출
            String jsonPart = extractJsonFromResponse(gptReply);
            GptResponse response = objectMapper.readValue(jsonPart, GptResponse.class);
            return response;
        } catch (JsonProcessingException e) {
            throw new GptResponseParseException("GPT 응답을 파싱하는 중 오류가 발생했습니다.", e);
        }
    }

    private String extractJsonFromResponse(String response) {
        // 응답에서 '{'로 시작하고 '}'로 끝나는 부분 추출
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}') + 1;
        if (startIndex >= 0 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex);
        }
        return response;
    }
}
