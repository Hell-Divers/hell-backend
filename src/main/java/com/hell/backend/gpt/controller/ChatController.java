package com.hell.backend.gpt.controller;

import com.hell.backend.common.security.CustomUserDetails;
import com.hell.backend.gpt.dto.GptRequest;
import com.hell.backend.gpt.dto.GptResponse;
import com.hell.backend.gpt.service.GptService;
import com.hell.backend.expense.service.BalanceService;
import com.hell.backend.common.dto.ErrorResponse;
import com.hell.backend.gpt.dto.ChatResponse;
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

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Controller", description = "채팅 관련 API")
public class ChatController {

    private final GptService gptService;
    private final BalanceService balanceService;
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
            Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();
            BigDecimal currentBalance = balanceService.getCurrentBalance(userId);
            
            // GPT 처리
            GptResponse gptResponse = gptService.processChatGptRequest(request, userId, currentBalance);
            
            // 디버깅 로�� 추가
            System.out.println("GPT Response received: " + objectMapper.writeValueAsString(gptResponse));
            
            // 내부 처리 (지출 정보 저장 등)
            if ("accept".equals(gptResponse.getState()) && gptResponse.getExpenses() != null) {
                for (GptResponse.ExpenseData expense : gptResponse.getExpenses()) {
                    try {
                        balanceService.updateBalance(
                            userId,
                            BigDecimal.valueOf(expense.getAmount()),
                            expense.getType()
                        );
                    } catch (Exception e) {
                        System.err.println("Failed to update balance: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            // 프론트엔드로는 메시지만 전달
            ChatResponse response = new ChatResponse();
            response.setMessage(gptResponse.getMessage());
            
            // 디버깅 로�� 추가
            System.out.println("Sending response: " + objectMapper.writeValueAsString(response));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in sendMessage: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse("처리 중 오류가 발생했습니다."));
        }
    }
}
