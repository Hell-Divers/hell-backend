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
    private String category;
    private BigDecimal amount;
    private String location;
    private LocalDateTime dateTime;
    private String balance;

    public ExpenseResponse(Expense expense) {
        this.id = expense.getId();
        this.category = expense.getCategory().getName();
        this.amount = expense.getAmount();
        this.location = expense.getLocation();
        this.dateTime = expense.getDateTime();
        this.balance = expense.getBalance();
    }
}
