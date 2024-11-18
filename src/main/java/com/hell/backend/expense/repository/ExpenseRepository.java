package com.hell.backend.expense.repository;

import com.hell.backend.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND MONTH(e.date) = :month AND YEAR(e.date) = :year AND e.deletedAt IS NULL")
    List<Expense> findByUserIdAndMonthAndYear(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

    List<Expense> findByUserIdAndDateAndDeletedAtIsNull(Long userId, LocalDate date);

    List<Expense> findByUserIdAndDeletedAtIsNull(Long userId);
}