package com.hell.backend.expense.dto;

import com.hell.backend.expense.entity.Expense;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExpenseResponse {
    private Long id;
    private String categoryName;
    private BigDecimal amount;
    private String location;
    private LocalDate date;
    private String memo;

    public ExpenseResponse(Expense expense) {
        this.id = expense.getId();
        this.categoryName = expense.getCategory().getName();
        this.amount = expense.getAmount();
        this.location = expense.getLocation();
        this.date = expense.getDate();
        this.memo = expense.getMemo();
    }
}
