package com.hell.backend.expense.service;

import com.hell.backend.expense.entity.Category;
import com.hell.backend.expense.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void initCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> defaultCategories = Arrays.asList(
                new Category("food", "expense"),
                new Category("transportation", "expense"),
                new Category("shopping", "expense"),
                new Category("entertainment", "expense"),
                new Category("health", "expense"),
                new Category("education", "expense"),
                new Category("utilities", "expense"),
                new Category("other", "expense"),
                new Category("salary", "income"),
                new Category("bonus", "income")
            );
            categoryRepository.saveAll(defaultCategories);
        }
    }
} 