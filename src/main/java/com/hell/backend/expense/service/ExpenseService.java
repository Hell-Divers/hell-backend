package com.hell.backend.expense.service;

import com.hell.backend.expense.dto.ExpenseRequest;
import com.hell.backend.expense.dto.ExpenseResponse;
import com.hell.backend.expense.dto.MonthlyExpenseResponse;
import com.hell.backend.expense.entity.Category;
import com.hell.backend.expense.entity.Expense;
import com.hell.backend.expense.repository.CategoryRepository;
import com.hell.backend.expense.repository.ExpenseRepository;
import com.hell.backend.gpt.dto.GptResponse;
import com.hell.backend.users.entity.User;
import com.hell.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BalanceService balanceService;

    @Transactional
    public ExpenseResponse addExpense(ExpenseRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setCategory(category);
        expense.setAmount(request.getAmount());
        expense.setLocation(request.getLocation());
        expense.setDateTime(request.getDateTime());

        balanceService.updateBalance(userId, request.getAmount(), category.getType());

        expenseRepository.save(expense);

        return new ExpenseResponse(expense);
    }

    @Transactional
    public void addExpenseFromGptData(GptResponse response, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (response.getExpenses() == null || response.getExpenses().isEmpty()) {
            throw new IllegalArgumentException("GPT response contains no valid data.");
        }

        for (GptResponse.ExpenseData expense : response.getExpenses()) {
            Category category = categoryRepository.findByName(expense.getCategory())
                    .orElseGet(() -> {
                        Category newCategory = new Category();
                        newCategory.setName(expense.getCategory());
                        newCategory.setType(expense.getType());
                        return categoryRepository.save(newCategory);
                    });

            Expense expenseEntity = new Expense();
            expenseEntity.setUser(user);
            expenseEntity.setCategory(category);
            expenseEntity.setAmount(BigDecimal.valueOf(expense.getAmount()));
            expenseEntity.setLocation(expense.getLocation());
            expenseEntity.setDateTime(parseDateTime(expense.getDatetime()));

            expenseRepository.save(expenseEntity);
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + dateTimeStr, e);
        }
    }

    public List<MonthlyExpenseResponse> getMonthlyExpenses(Long userId, int year, int month) {
        List<Expense> expenses = expenseRepository.findByUserIdAndMonthAndYear(userId, month, year);
        Map<LocalDate, MonthlyExpenseResponse> expenseMap = new HashMap<>();

        for (Expense expense : expenses) {
            LocalDate date = expense.getDateTime().toLocalDate();
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
        return expenses.stream()
                .map(ExpenseResponse::new)
                .collect(Collectors.toList());
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
