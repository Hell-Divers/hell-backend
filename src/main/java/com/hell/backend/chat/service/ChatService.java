package com.hell.backend.chat.service;

import com.hell.backend.chat.entity.ChatMessage;
import com.hell.backend.chat.entity.MessageRole;
import com.hell.backend.chat.repository.ChatMessageRepository;
import com.hell.backend.gpt.dto.GptRequest;
import com.hell.backend.users.entity.User;
import com.hell.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void saveChatMessage(GptRequest.Message message, Long userId, String state) {
        System.out.println("=== ChatService.saveChatMessage ===");
        System.out.println("Message: " + message.getContent());
        System.out.println("UserId: " + userId);
        System.out.println("State: " + state);
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            System.out.println("User found: " + user.getEmail());

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setUser(user);
            chatMessage.setRole(MessageRole.valueOf(message.getRole().toUpperCase()));
            chatMessage.setContent(message.getContent());
            chatMessage.setState(state);
            
            // datetime 처리
            if (message.getDatetime() != null) {
                System.out.println("Parsing datetime: " + message.getDatetime());
                chatMessage.setMessageDateTime(LocalDateTime.parse(
                    message.getDatetime(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime());
            } else {
                System.out.println("Using current datetime");
                chatMessage.setMessageDateTime(LocalDateTime.now());
            }
            
            chatMessageRepository.save(chatMessage);
            System.out.println("Chat message saved successfully");
            
        } catch (Exception e) {
            System.err.println("=== Error in ChatService ===");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 