package com.hell.backend.expense.service;


import com.hell.backend.expense.dto.ExpenseRequest;
import com.hell.backend.expense.dto.ExpenseResponse;
import com.hell.backend.expense.dto.MonthlyExpenseResponse;
import com.hell.backend.expense.entity.Category;
import com.hell.backend.expense.entity.Expense;
import com.hell.backend.expense.repository.CategoryRepository;
import com.hell.backend.expense.repository.ExpenseRepository;
import com.hell.backend.users.entity.User;
import com.hell.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseResponse addExpense(ExpenseRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category"));

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setCategory(category);
        expense.setAmount(request.getAmount());
        expense.setLocation(request.getLocation());
        expense.setDate(request.getDate());
        expense.setMemo(request.getMemo());
        expenseRepository.save(expense);

        return new ExpenseResponse(expense);
    }

    public List<MonthlyExpenseResponse> getMonthlyExpenses(Long userId, int year, int month) {
        List<Expense> expenses = expenseRepository.findByUserIdAndMonthAndYear(userId, month, year);

        // 날짜별로 수입과 지출을 합산
        Map<LocalDate, MonthlyExpenseResponse> expenseMap = new HashMap<>();

        for (Expense expense : expenses) {
            LocalDate date = expense.getDate();
            MonthlyExpenseResponse response = expenseMap.getOrDefault(date, new MonthlyExpenseResponse(date));

            if ("income".equals(expense.getCategory().getType())) {
                response.addIncome(expense.getAmount());
            } else {
                response.addExpenses(expense.getAmount());
            }

            expenseMap.put(date, response);
        }

        return new ArrayList<>(expenseMap.values());
    }

    public List<ExpenseResponse> getDailyExpenses(Long userId, LocalDate date) {
        List<Expense> expenses = expenseRepository.findByUserIdAndDateAndDeletedAtIsNull(userId, date);
        List<ExpenseResponse> responseList = new ArrayList<>();
        for (Expense expense : expenses) {
            responseList.add(new ExpenseResponse(expense));
        }
        return responseList;
    }

    public List<ExpenseResponse> getAllExpenses(Long userId) {
        List<Expense> expenses = expenseRepository.findByUserIdAndDeletedAtIsNull(userId);
        List<ExpenseResponse> responseList = new ArrayList<>();
        for (Expense expense : expenses) {
            responseList.add(new ExpenseResponse(expense));
        }
        return responseList;
    }
}
