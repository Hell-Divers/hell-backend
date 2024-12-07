package com.hell.backend.expense.service;

import com.hell.backend.expense.entity.Category;
import com.hell.backend.expense.entity.Expense;
import com.hell.backend.expense.repository.CategoryRepository;
import com.hell.backend.expense.repository.ExpenseRepository;
import com.hell.backend.users.entity.User;
import com.hell.backend.users.repository.UserRepository;
import com.hell.backend.expense.service.BalanceService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestDataInitializer {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;

    @PostConstruct
    @Transactional
    public void initializeTestData() {
        try {
            // 1. 테고리 초기화
            if (categoryRepository.count() == 0) {
                initializeCategories();
            }

            // 2. 테스트 사용자 생성
            User user = userRepository.findByEmail("user@example.com")
                    .orElseGet(this::createTestUser);

            // 3. 기존 지출 데이터 삭제
            expenseRepository.deleteAllByUserId(user.getId());

            // 4. 초기 잔액 설정
            balanceService.updateBalance(user.getId(), new BigDecimal("1000000"), "income");

            // 5. 테스트 데이터 생성
            createTestExpenses(user);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeCategories() {
        List<Category> categories = Arrays.asList(
            new Category("food", "expense"),
            new Category("transportation", "expense"),
            new Category("shopping", "expense")
        );
        categoryRepository.saveAll(categories);
    }

    private void createTestExpenses(User user) {
        Category food = categoryRepository.findByName("food").orElseThrow();
        Category transportation = categoryRepository.findByName("transportation").orElseThrow();
        Category shopping = categoryRepository.findByName("shopping").orElseThrow();

        List<ExpenseData> testData = Arrays.asList(
            // 12월 5일 데이터
            new ExpenseData(food, "15000", "스타벅스", "2024-12-05 09:00:00"),
            new ExpenseData(transportation, "5000", "택시", "2024-12-05 13:30:00"),
            new ExpenseData(shopping, "50000", "유니클로", "2024-12-05 17:00:00"),
            
            // 12월 6일 데이터
            new ExpenseData(food, "8000", "이디야", "2024-12-06 10:00:00"),
            new ExpenseData(transportation, "1500", "버스", "2024-12-06 14:00:00"),
            new ExpenseData(shopping, "35000", "H&M", "2024-12-06 18:30:00"),
            
            // 12월 7일 데이터
            new ExpenseData(food, "12000", "투썸플레이스", "2024-12-07 11:00:00"),
            new ExpenseData(transportation, "3000", "지하철", "2024-12-07 15:30:00"),
            new ExpenseData(shopping, "45000", "자라", "2024-12-07 19:00:00")
        );

        for (ExpenseData data : testData) {
            createExpense(user, data);
        }
    }

    @Getter
    @AllArgsConstructor
    private static class ExpenseData {
        private Category category;
        private String amount;
        private String location;
        private String dateTime;
    }

    private Category getOrCreateCategory(String name, String type) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name);
                    category.setType(type);
                    return categoryRepository.save(category);
                });
    }

    private void createExpense(User user, ExpenseData data) {
        Expense expense = new Expense();
        expense.setUser(user);
        expense.setCategory(data.getCategory());
        expense.setAmount(new BigDecimal(data.getAmount()));
        expense.setLocation(data.getLocation());
        expense.setDateTime(LocalDateTime.parse(data.getDateTime().replace(" ", "T")));
        expenseRepository.save(expense);
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("$2a$10$iWD/lEGq6KV8zkR.Xc4jB.HKhYN9zCGS0ANrPnYWUZu7w4yR0UxXO"); // "password123"
        user.setNickname("TestUser");
        return userRepository.save(user);
    }
} 