package com.hell.backend.gpt.repository;

import com.hell.backend.gpt.entity.GptConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GptConversationRepository extends JpaRepository<GptConversation, Long> {
    List<GptConversation> findByUserId(Long userId);
}
