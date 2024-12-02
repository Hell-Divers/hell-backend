package com.hell.backend.gpt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hell.backend.expense.service.ExpenseService;
import com.hell.backend.gpt.dto.GptRequest;
import com.hell.backend.gpt.dto.GptRequest.Message;
import com.hell.backend.gpt.dto.GptResponse;
import com.hell.backend.gpt.entity.GptConversation;
import com.hell.backend.gpt.exception.GptResponseParseException;
import com.hell.backend.gpt.repository.GptConversationRepository;
import com.hell.backend.users.entity.User;
import com.hell.backend.users.repository.UserRepository;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GptService {

    private final OpenAIClient openAIClient;
    private final GptConversationRepository gptConversationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExpenseService expenseService; // ExpenseService 주입

    public GptResponse processChatGptRequest(GptRequest request, Long userId) {

        // OpenAI API 호출을 위한 메시지 생성
        List<Map<String, String>> messages = createMessages(request);

        // GPT로부터 응답 받기
        String gptReply;
        try {
            gptReply = openAIClient.getChatResponse(messages);
            // GPT 응답을 로그로 출력
            System.out.println("GPT Reply: " + gptReply);
        } catch (Exception e) {
            // 기타 예외 처리
            throw new RuntimeException("GPT 응답을 받는 중 오류가 발생했습니다.", e);
        }

        // GPT 응답을 파싱하여 GptResponse 객체로 변환
        GptResponse response;
        try {
            response = parseGptResponse(gptReply);
        } catch (GptResponseParseException e) {
            // 파싱 예외 처리
            response = new GptResponse();
            response.setRole("assistant");
            GptResponse.Content content = new GptResponse.Content();
            content.setMessage("죄송합니다, 응답을 처리하는 중 오류가 발생했습니다. 다시 시도해 주세요.");
            response.setContent(content);
            response.setState("error");
            // 로그를 남기거나 추가 처리 가능
            System.err.println("GPT 응답 파싱 오류: " + e.getMessage());
        }

        // 대화 내역 저장
        saveConversation(request, gptReply, userId);

        // 필요한 경우 데이터 저장 로직 추가
        if ("accept".equals(response.getState()) && response.getContent().getValues() != null) {
            expenseService.addExpenseFromGptData(response, userId);
        }

        return response;
    }

    private List<Map<String, String>> createMessages(GptRequest request) {
        // 요청으로부터 사용자의 메시지 가져오기
        List<Message> userMessages = request.getMessages();

        // 시스템 메시지 생성
        Map<String, String> systemMessage = Map.of(
                "role", "system",
                "content", GptPrompt.CHAT_PROMPT
        );


        // 사용자 메시지와 시스템 메시지 결합
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(systemMessage);

        for (Message msg : userMessages) {
            messages.add(Map.of(
                    "role", msg.getRole(),
                    "content", msg.getContent()
            ));
        }

        return messages;
    }

    // 대화 내역을 데이터베이스에 저장
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