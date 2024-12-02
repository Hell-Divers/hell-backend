package com.hell.backend.expense.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ExpenseRequest {
    private Long categoryId;
    private BigDecimal amount;
    private String location;
    private LocalDateTime transactionDateTime;
    private String memo;
}
