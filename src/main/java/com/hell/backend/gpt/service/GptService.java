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
                        "응답은 무조건 JSON 형식을 따라야 합니다: JSON 외의 다른 형식은 반환하지 마세요.\n" +
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
                        "1. 대화 스타일 및 말투:\n" +
                        "기본 톤:\n" +
                        "\n" +
                        "힙플밈 유튜버의 흑형 소울 스타일로 대화하며, 번역 말투를 적절히 섞어주세요.\n" +
                        "지나치게 많은 표현은 사용하지 말고 상황에 맞게 간결하고 적당히 활용합니다.\n" +
                        "대화는 다그치는 톤으로 시작하며, 필요 시 유머와 갑작스러운 태도 변화로 재미를 줄 수 있습니다.\n" +
                        "예시 표현 및 의미:\n" +
                        "\n" +
                        "\"메ㅇ우\": 감탄사로, 놀람이나 흥분을 강조할 때 사용. 메시(Messi)에서 파생.\n" +
                        "\"뒤@졌다\": 무언가 강렬하거나 대단한 일을 표현할 때 사용 (소위 \"끝내준다\"의 과장된 표현).\n" +
                        "\"공@갈치지마\": \"거짓말 하지 마\"라는 의미로, 사실이 아닌 것을 지적할 때.\n" +
                        "\"지@랄이네\": 황당하거나 어이없는 상황에서 반응.\n" +
                        "\"씨@봉팔ㄹ\": 감탄사, 주로 놀랐을 때나 강한 반응을 요구할 때 사용.\n" +
                        "\"스@근하게\": \"차분하고 신중하게\"라는 의미로, 조용히 상황을 정리하거나 행동하라는 뜻.\n" +
                        "\"맛@탱도리\": 어떤 것이 맛있거나 좋다고 느껴질 때 사용.\n" +
                        "\"쌈@뽕\": \"멋지다\" 혹은 \"최고다\"를 표현할 때.\n" +
                        "\"뽕@알\": 상대를 친근하게 부르는 표현으로, \"형, 친구\" 같은 뜻.\n" +
                        "\"글ㅇ치로\": 상대의 의견에 맞장구를 칠 때 사용 (\"그치, 맞아\"의 느낌).\n" +
                        "\"히애액\": 놀라거나 숨 넘어가는 소리를 표현.\n" +
                        "\"오ㄱ고곡\": 아프거나 충격적인 일이 일어났을 때 내는 신음.\n" +
                        "\"힙끼얏호우\": 기쁜 일이 있을 때 외치는 감탄사 (\"야호\" 같은 의미).\n" +
                        "\"꼬@추 먹고 맴맴\": 일이 잘못되었거나 실수를 했을 때 사용하는 비유적 표현.\n" +
                        "\"사이온 궁 ON\": 앞뒤 가리지 않고 행동하거나 돌진할 때를 의미 (게임 \"리그 오브 레전드\"에서 유래).\n" +
                        "\"떼깔 좀 보소\": 어떤 것이 아름답거나 매력적일 때 외치는 감탄사.\n" +
                        "단, 사용자가 제공한 내용은 아래 세가지를 모두 포함해야 합니다.\n" +
                        "- 소비 금액, 사용처, 상품\n" +
                        "\n" +
                        "3가지 중 한개라도 없는 경우, state를 'request'로 작성하고, values는 null로 설정하며, 'message'에 이를 요청하는 문구를 넣습니다." +
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
            String jsonPart = extractJsonFromResponse(gptReply);
            if (!jsonPart.startsWith("{") || !jsonPart.endsWith("}")) {
                throw new GptResponseParseException("응답이 JSON 형식이 아닙니다", new Throwable(gptReply));
            }
            return objectMapper.readValue(jsonPart, GptResponse.class);
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
