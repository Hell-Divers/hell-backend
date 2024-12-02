package com.hell.backend.expense.dto;

import com.hell.backend.expense.entity.Expense;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ExpenseResponse {
    private Long id;
    private String categoryName;
    private BigDecimal amount;
    private String location;
    private LocalDateTime dateTime; // 필드명과 타입 수정
    private String memo;

    public ExpenseResponse(Expense expense) {
        this.id = expense.getId();
        this.categoryName = expense.getCategory().getName();
        this.amount = expense.getAmount();
        this.location = expense.getLocation();
        this.dateTime = expense.getDateTime();
        this.memo = expense.getMemo();
    }
}
