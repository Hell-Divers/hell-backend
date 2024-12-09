package com.hell.backend.gpt.controller;

import com.hell.backend.common.dto.ErrorResponse;
import com.hell.backend.common.security.CustomUserDetails;
import com.hell.backend.expense.service.BalanceService;
import com.hell.backend.gpt.dto.GptRequest;
import com.hell.backend.gpt.util.GptResponseParser;
import com.hell.backend.gpt.service.TestGptService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Controller", description = "채팅 관련 API")
@Slf4j
public class ChatController {

    private final TestGptService testGptService;
    private final BalanceService balanceService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "채팅 메시지 전송", description = "사용자의 채팅 메시지를 처리하고 GPT의 응답을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 처리되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "401", description = "인증 실패입니다.")
    })
    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody GptRequest request, Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            BigDecimal currentBalance = balanceService.getCurrentBalance(userId);

            // 요청 로깅
            log.info("Request: {}", objectMapper.writeValueAsString(request));

            // GPT 응답 처리
            String gptResponse = testGptService.sendChatRequest(request.getMessages(), currentBalance);

            // GPT 응답 로깅
            log.info("GPT Response: {}", gptResponse);

            // GptResponseParser를 사용하여 content와 state만 추출
            String parsedResponse = GptResponseParser.extractContentAndState(gptResponse);

            // 파싱된 응답 로깅
            log.info("Parsed Response: {}", parsedResponse);

            // 파싱된 JSON 문자열을 JsonNode로 변환하여 state 확인
            JsonNode responseNode = objectMapper.readTree(parsedResponse);
            String state = responseNode.path("state").asText();

            // state가 accept인 경우에만 지출 정보 저장
            /*
            if ("accept".equals(state)) {
                JsonNode expensesNode = responseNode.get("expenses");
                if (expensesNode != null && expensesNode.isArray()) {
                    for (JsonNode expense : expensesNode) {
                        balanceService.updateBalance(
                            userId,
                            BigDecimal.valueOf(expense.get("amount").asDouble()),
                            expense.get("type").asText()
                        );
                    }
                }
            }
            */

            // 파싱된 응답(JSON 문자열)을 그대로 반환
            return ResponseEntity.ok(parsedResponse);

        } catch (Exception e) {
            log.error("Chat processing error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("처리 중 오류가 발생했습니다.");
        }
    }
}
