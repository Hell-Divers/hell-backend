package com.hell.backend.expense.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MonthlyExpenseResponse {
    private LocalDate date;
    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal expenses = BigDecimal.ZERO;

    public MonthlyExpenseResponse(LocalDate date) {
        this.date = date;
    }

    public void addIncome(BigDecimal amount) {
        income = income.add(amount);
    }

    public void addExpenses(BigDecimal amount) {
        expenses = expenses.add(amount);
    }
}
