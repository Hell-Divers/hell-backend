package com.hell.backend.gpt.controller;

import com.hell.backend.common.security.CustomUserDetails;
import com.hell.backend.gpt.dto.GptRequest;
import com.hell.backend.gpt.dto.GptResponse;
import com.hell.backend.gpt.service.GptService;
import com.hell.backend.expense.service.BalanceService;
import com.hell.backend.common.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Controller", description = "채팅 관련 API")
public class ChatController {

    private final GptService gptService;
    private final BalanceService balanceService;

    @Operation(summary = "채팅 메시지 전송", description = "사용자의 채팅 메시지를 처리하고 GPT의 응답을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 처리되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "401", description = "인증 실패입니다.")
    })
    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody GptRequest request, Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            
            // 현재 잔액 조회
            BigDecimal currentBalance = balanceService.getCurrentBalance(userId);
            
            // GPT 요청 처리
            GptResponse response = gptService.processChatGptRequest(request, userId, currentBalance);
            
            // state가 accept인 경우에만 지출 정보 저장
            if ("accept".equals(response.getState()) && response.getExpenses() != null) {
                for (GptResponse.ExpenseData expense : response.getExpenses()) {
                    balanceService.updateBalance(
                        userId,
                        BigDecimal.valueOf(expense.getAmount()),
                        expense.getType()
                    );
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("처리 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
