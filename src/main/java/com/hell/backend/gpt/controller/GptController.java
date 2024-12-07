package com.hell.backend.gpt.controller;

import com.hell.backend.gpt.service.GptService;
import com.hell.backend.balance.service.BalanceService;
import com.hell.backend.common.ErrorResponse;
import com.hell.backend.common.GptRequest;
import com.hell.backend.common.GptResponse;
import com.hell.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/gpt")
@RequiredArgsConstructor
@Tag(name = "GPT Controller", description = "GPT 통신 관련 API")
public class GptController {

    private final GptService gptService;
    private final BalanceService balanceService;

    @Operation(summary = "GPT 요청 처리", description = "GPT API를 호출하고 결과를 반환합니다.")
    @PostMapping("/process")
    public ResponseEntity<?> processGptRequest(
            @RequestBody GptRequest request,
            Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            BigDecimal currentBalance = balanceService.getCurrentBalance(userId);
            
            GptResponse gptResponse = gptService.processChatGptRequest(
                request,
                userId,
                currentBalance
            );
            
            // state가 accept인 경우에만 지출 정보 저장
            if ("accept".equals(gptResponse.getState()) && gptResponse.getExpenses() != null) {
                for (GptResponse.ExpenseData expense : gptResponse.getExpenses()) {
                    balanceService.updateBalance(
                        userId,
                        BigDecimal.valueOf(expense.getAmount()),
                        expense.getType()
                    );
                }
            }
            
            return ResponseEntity.ok(gptResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("GPT 처리 오류가 발생했습니다: " + e.getMessage()));
        }
    }
} 