package com.hell.backend.gpt.controller;

import com.hell.backend.common.security.CustomUserDetails;
import com.hell.backend.gpt.dto.GptRequest;
import com.hell.backend.gpt.dto.GptResponse;
import com.hell.backend.gpt.service.GptService;
import com.hell.backend.expense.service.BalanceService;
import com.hell.backend.gpt.dto.ChatResponse;
import com.hell.backend.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Controller", description = "채팅 관련 API")
@Slf4j
public class ChatController {

    private final GptService gptService;
    private final BalanceService balanceService;
    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "채팅 메시지 전송", description = "사용자의 채팅 메시지를 처리하고 GPT의 응답을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 처리되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "401", description = "인증 실패입니다.")
    })
    @PostMapping
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody GptRequest request, Authentication auth) {
        try {
            log.debug("Received chat request: {}", request);
            
            // 인증 확인
            if (auth == null || auth.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ChatResponse("인증이 필요합니다."));
            }

            System.out.println("=== Request received ===");
            System.out.println("Request body: " + objectMapper.writeValueAsString(request));

            Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();
            BigDecimal currentBalance = balanceService.getCurrentBalance(userId);
            System.out.println("Current balance: " + currentBalance);
            
            // 사용자 메시지 저장
            System.out.println("=== Saving user messages ===");
            for (GptRequest.Message message : request.getMessages()) {
                System.out.println("Saving message: " + objectMapper.writeValueAsString(message));
                chatService.saveChatMessage(message, userId, "request");
            }
            
            // GPT 처리
            System.out.println("=== Processing GPT request ===");
            GptResponse gptResponse = gptService.processChatGptRequest(request, userId, currentBalance);
            log.debug("GPT response: {}", gptResponse);
            System.out.println("GPT Response: " + objectMapper.writeValueAsString(gptResponse));
            
            // 지출 정보 저장
            if ("accept".equals(gptResponse.getState()) && gptResponse.getExpenses() != null) {
                System.out.println("=== Processing expenses ===");
                for (GptResponse.ExpenseData expense : gptResponse.getExpenses()) {
                    System.out.println("Processing expense: " + objectMapper.writeValueAsString(expense));
                    try {
                        balanceService.updateBalance(
                            userId,
                            BigDecimal.valueOf(expense.getAmount()),
                            expense.getType()
                        );
                        System.out.println("Balance updated successfully");
                    } catch (Exception e) {
                        System.err.println("Failed to update balance: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            // GPT 응답 저장
            System.out.println("=== Saving GPT response ===");
            GptRequest.Message gptMessage = new GptRequest.Message();
            gptMessage.setRole("assistant");
            gptMessage.setContent(gptResponse.getMessage());
            gptMessage.setDatetime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            chatService.saveChatMessage(gptMessage, userId, gptResponse.getState());
            
            // 응답 생성
            ChatResponse response = new ChatResponse();
            response.setMessage(gptResponse.getMessage());
            System.out.println("=== Sending response ===");
            System.out.println("Response: " + objectMapper.writeValueAsString(response));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Chat processing error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse("처리 중 오류가 발생했습니다."));
        }
    }
}
