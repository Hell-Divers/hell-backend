package com.hell.backend.gpt.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class GptResponse {
    private String message;
    private String state;
    private List<ExpenseData> expenses;

    @Getter
    @Setter
    @ToString
    public static class ExpenseData {
        private double amount;
        private String datetime;
        private String category;
        private String location;
        private String type;
    }
}
