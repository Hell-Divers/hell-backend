package com.hell.backend.chat.service;

import com.hell.backend.chat.entity.ChatMessage;
import com.hell.backend.chat.entity.MessageRole;
import com.hell.backend.chat.repository.ChatMessageRepository;
import com.hell.backend.expense.service.BalanceService;
import com.hell.backend.gpt.dto.GptRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    
    @Transactional
    public void saveChatMessage(GptRequest.Message message, Long userId, String state) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRole(MessageRole.valueOf(message.getRole().toUpperCase()));
        chatMessage.setContent(message.getContent());
        chatMessage.setState(state);
        
        // KST 기준으�� 시간 변환
        chatMessage.setMessageDateTime(LocalDateTime.parse(
            message.getDatetime(), 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        ).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime());
        
        chatMessageRepository.save(chatMessage);
    }
} 