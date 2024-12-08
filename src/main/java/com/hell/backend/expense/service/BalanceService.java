package com.hell.backend.expense.service;

import com.hell.backend.expense.entity.Balance;
import com.hell.backend.expense.repository.BalanceRepository;
import com.hell.backend.users.entity.User;
import com.hell.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BalanceService {
    private final BalanceRepository balanceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(Long userId) {
        return balanceRepository.findByUserId(userId)
                .map(Balance::getCurrentBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public void updateBalance(Long userId, BigDecimal amount, String type) {
        System.out.println("=== BalanceService.updateBalance ===");
        System.out.println("UserId: " + userId);
        System.out.println("Amount: " + amount);
        System.out.println("Type: " + type);
        
        try {
            Balance balance = balanceRepository.findByUserId(userId)
                    .orElseGet(() -> createInitialBalance(userId));
            System.out.println("Current balance: " + balance.getCurrentBalance());

            BigDecimal newBalance;
            if ("expense".equals(type)) {
                newBalance = balance.getCurrentBalance().subtract(amount);
            } else if ("income".equals(type)) {
                newBalance = balance.getCurrentBalance().add(amount);
            } else {
                throw new IllegalArgumentException("Invalid transaction type: " + type);
            }
            
            System.out.println("New balance: " + newBalance);
            balance.setCurrentBalance(newBalance);
            balance.setLastUpdated(LocalDateTime.now());
            balanceRepository.save(balance);
            System.out.println("Balance updated successfully");
            
        } catch (Exception e) {
            System.err.println("=== Error in BalanceService ===");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private Balance createInitialBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Balance balance = new Balance();
        balance.setUser(user);
        balance.setCurrentBalance(BigDecimal.ZERO);
        balance.setLastUpdated(LocalDateTime.now());
        return balanceRepository.save(balance);
    }
} 