package com.hell.backend.expense.repository;

import com.hell.backend.expense.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 필요한 경우 추가 메서드 정의
}