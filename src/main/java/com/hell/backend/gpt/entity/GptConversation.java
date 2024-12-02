package com.hell.backend.gpt.entity;

import com.hell.backend.users.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gpt_conversations")
@Getter
@Setter
@NoArgsConstructor
public class GptConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User와의 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_message", columnDefinition = "TEXT")
    private String userMessage;

    @Column(name = "gpt_response", columnDefinition = "TEXT")
    private String gptResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
