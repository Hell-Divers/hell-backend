package com.hell.backend.gpt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
                "content", "당신은 사용자의 지출 내역을 분석하여 JSON 형식으로 구조화된 데이터를 반환하는 역할을 합니다. " +
                        "message 필드에는 지출 내역을 기반으로 '힙플밈' 스타일의 유머 메시지를 작성하세요. " +
                        "응답은 다음의 JSON 형식을 따라야 합니다:\n" +
                        "{\n" +
                        "  \"role\": \"assistant\",\n" +
                        "  \"content\": {\n" +
                        "    \"message\": \"힙플밈 스타일 메시지\",\n" +
                        "    \"values\": [\n" +
                        "      {\n" +
                        "        \"datetime\": \"YYYY-MM-DDTHH:MM:SSZ\",\n" +
                        "        \"type\": \"expense\",\n" +
                        "        \"amount\": 금액,\n" +
                        "        \"category\": \"카테고리\",\n" +
                        "        \"place\": \"사용처\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"state\": \"accept\"\n" +
                        "}\n" +
                        "message 필드는 소비 내역에 맞게 힙플밈 스타일의 표현을 포함해야 합니다. 예를 들어:\n" +
                        "- '딸@랑구' 또는 '맛@탱도리'는 음식 카테고리에 적합합니다.\n" +
                        "- '스@근하게'는 교통 카테고리에 적합합니다.\n" +
                        "- 금액이 크면 과장된 반응 ('씨@봉방거, 이렇게 써도 된다고?')을 사용하고, 금액이 작으면 가벼운 유머 ('스@근하게 썼네~')를 사용하세요.\n" +
                        "추가적인 설명이나 텍스트는 포함하지 마세요."
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

    private void saveConversation(GptRequest request, String gptReply, Long userId) {
        // 대화 내역을 데이터베이스에 저장
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
            // 응답에서 JSON 부분만 추출
            String jsonPart = extractJsonFromResponse(gptReply);
            GptResponse response = objectMapper.readValue(jsonPart, GptResponse.class);

            // 필수 필드 검증 및 추가 로직
            // ...

            return response;
        } catch (JsonProcessingException e) {
            // JSON 파싱 예외 처리
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
