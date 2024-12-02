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

    /**
     * 프론트엔드에서 전달된 ExpenseRequest를 기반으로 지출을 추가합니다.
     *
     * @param request ExpenseRequest 객체
     * @param userId  사용자 ID
     * @return 추가된 지출의 ExpenseResponse
     */
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
        expense.setDateTime(request.getDateTime()); // date 필드를 dateTime으로 변경
        expense.setMemo(request.getMemo());
        expenseRepository.save(expense);

        return new ExpenseResponse(expense);
    }

    /**
     * GPT의 응답 데이터를 기반으로 지출을 추가합니다.
     *
     * @param response GptResponse 객체
     * @param userId   사용자 ID
     */
    public void addExpenseFromGptData(GptResponse response, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (response.getContent().getValues() != null) {
            for (GptResponse.Data data : response.getContent().getValues()) {
                // 카테고리 조회 또는 생성
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

                // 날짜 문자열 파싱
                LocalDateTime dateTime = parseDateTime(data.getDatetime());
                expense.setDateTime(dateTime);

                expense.setMemo(""); // 필요 시 data에서 가져오기

                expenseRepository.save(expense);
            }
        } else {
            // 처리할 데이터가 없음
            System.err.println("GptResponse에 데이터가 없습니다.");
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        // 날짜 형식에 따라 DateTimeFormatter 설정
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return LocalDateTime.parse(dateTimeStr, formatter);
    }
    /**
     * 월별 지출 내역을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param year   연도
     * @param month  월
     * @return 월별 지출 내역 리스트
     */
    public List<MonthlyExpenseResponse> getMonthlyExpenses(Long userId, int year, int month) {
        List<Expense> expenses = expenseRepository.findByUserIdAndMonthAndYear(userId, month, year);

        // 날짜별로 수입과 지출을 합산
        Map<LocalDate, MonthlyExpenseResponse> expenseMap = new HashMap<>();

        for (Expense expense : expenses) {
            LocalDate date = expense.getDateTime().toLocalDate(); // 수정된 부분
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

    /**
     * 일별 지출 내역을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param date   날짜
     * @return 일별 지출 내역 리스트
     */
    public List<ExpenseResponse> getDailyExpenses(Long userId, LocalDate date) {
        List<Expense> expenses = expenseRepository.findByUserIdAndDateAndDeletedAtIsNull(userId, date);
        List<ExpenseResponse> responseList = new ArrayList<>();
        for (Expense expense : expenses) {
            responseList.add(new ExpenseResponse(expense));
        }
        return responseList;
    }

    /**
     * 모든 지출 내역을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 지출 내역 리스트
     */
    public List<ExpenseResponse> getAllExpenses(Long userId) {
        List<Expense> expenses = expenseRepository.findByUserIdAndDeletedAtIsNull(userId);
        List<ExpenseResponse> responseList = new ArrayList<>();
        for (Expense expense : expenses) {
            responseList.add(new ExpenseResponse(expense));
        }
        return responseList;
    }
}
