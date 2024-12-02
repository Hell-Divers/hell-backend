package com.hell.backend.expense.controller;

import com.hell.backend.common.security.CustomUserDetails;
import com.hell.backend.expense.dto.ExpenseRequest;
import com.hell.backend.expense.dto.ExpenseResponse;
import com.hell.backend.expense.dto.MonthlyExpenseResponse;
import com.hell.backend.expense.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(@RequestBody ExpenseRequest request, Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        ExpenseResponse response = expenseService.addExpense(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyExpenseResponse>> getMonthlyExpenses(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        List<MonthlyExpenseResponse> expenses = expenseService.getMonthlyExpenses(userId, year, month);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/daily")
    public ResponseEntity<List<ExpenseResponse>> getDailyExpenses(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        LocalDate date = LocalDate.of(year, month, day);
        List<ExpenseResponse> expenses = expenseService.getDailyExpenses(userId, date);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        List<ExpenseResponse> expenses = expenseService.getAllExpenses(userId);
        return ResponseEntity.ok(expenses);
    }
}
