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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setCategory(category);
        expense.setAmount(request.getAmount());
        expense.setLocation(request.getLocation());
        expense.setDateTime(request.getDateTime());

        expenseRepository.save(expense);

        return new ExpenseResponse(expense);
    }

    public void addExpenseFromGptData(GptResponse response, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (response.getContent().getValues() == null || response.getContent().getValues().isEmpty()) {
            throw new IllegalArgumentException("GPT response contains no valid data.");
        }

        for (GptResponse.Data data : response.getContent().getValues()) {
            Category category = categoryRepository.findByName(data.getCategory())
                    .orElseGet(() -> {
                        Category newCategory = new Category();
                        newCategory.setName(data.getCategory());
                        newCategory.setType(data.getType());
                        return categoryRepository.save(newCategory);
                    });

            Expense expense = new Expense();
            expense.setUser(user);
            expense.setCategory(category);
            expense.setAmount(BigDecimal.valueOf(data.getAmount()));
            expense.setLocation(data.getPlace());
            expense.setDateTime(parseDateTime(data.getDatetime()));

            expenseRepository.save(expense);
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
