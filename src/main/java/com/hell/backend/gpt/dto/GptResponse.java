package com.hell.backend.gpt.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GptResponse {
    private String message;    // GPT의 응답 메시지
    private String state;      // "accept" 또는 "request"
    private List<ExpenseData> expenses;  // 지출 데이터 (있는 경우)

    @Getter
    @Setter
    public static class ExpenseData {
        private double amount;
        private String datetime;
        private String category;
        private String location;
        private String type;
    }
}
