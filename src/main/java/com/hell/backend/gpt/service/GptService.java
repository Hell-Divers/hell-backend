package com.hell.backend.gpt.service;

import com.hell.backend.gpt.dto.GptRequest;
import com.hell.backend.gpt.entity.GptConversation;
import com.hell.backend.gpt.repository.GptConversationRepository;
import com.hell.backend.users.entity.User;
import com.hell.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GptService {

    private final OpenAIClient openAIClient;
    private final GptConversationRepository gptConversationRepository;
    private final UserRepository userRepository;

    public String processChatGptRequest(GptRequest request, Long userId) {
        // OpenAI API 호출
        String gptResponse = openAIClient.getResponse(request.getModel(), request.getMessages());

        // 대화 내역 저장
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        GptConversation conversation = new GptConversation();
        conversation.setUser(user);
        conversation.setUserMessage(extractUserMessage(request));
        conversation.setGptResponse(gptResponse);
        gptConversationRepository.save(conversation);

        return gptResponse;
    }

    private String extractUserMessage(GptRequest request) {
        // 가장 최근의 사용자 메시지를 추출
        for (int i = request.getMessages().size() - 1; i >= 0; i--) {
            GptRequest.Message message = request.getMessages().get(i);
            if ("user".equals(message.getRole())) {
                return message.getContent();
            }
        }
        return "";
    }
}
