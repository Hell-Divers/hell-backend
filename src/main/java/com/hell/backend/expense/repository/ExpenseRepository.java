package com.hell.backend.expense.repository;

import com.hell.backend.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e JOIN FETCH e.category WHERE e.user.id = :userId AND DATE(e.dateTime) = :date AND e.deletedAt IS NULL")
    List<Expense> findByUserIdAndDateAndDeletedAtIsNull(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT e FROM Expense e JOIN FETCH e.category WHERE e.user.id = :userId AND MONTH(e.dateTime) = :month AND YEAR(e.dateTime) = :year AND e.deletedAt IS NULL")
    List<Expense> findByUserIdAndMonthAndYear(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT e FROM Expense e JOIN FETCH e.category WHERE e.user.id = :userId AND e.deletedAt IS NULL")
    List<Expense> findByUserIdAndDeletedAtIsNull(Long userId);

    void deleteAllByUserId(Long userId);
}
