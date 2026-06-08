package com.AIExpense.ExpenseTracker.expense.repository;

import com.AIExpense.ExpenseTracker.expense.entity.Expense;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    List<Expense> findByUserId(Long userId);

    List<Expense> findByCategoryAndUserId(ExpenseCategory category, Long userId);

    List<Expense> findByUserIdAndExpenseDateBetween(Long userId,
                                                    LocalDate startDate,
                                                    LocalDate endDate);

    @Query("SELECT e FROM Expense e where e.user.id = :userId " +
            "AND MONTH(e.expenseDate) = :month " +
            "AND YEAR(e.expenseDate) = :year")
    List<Expense> findByUserIdAndMonthAndYear(@Param("userId") Long userId,
                                              @Param("month") int month,
                                              @Param("year") int year);

    @Query("SELECT SUM(e.amount) FROM Expense e where " +
            "e.user.id = :userId " +
            "AND e.category = :category")
    BigDecimal getTotalAmountByUserIdAndCategory(@Param("userId") Long userId,
                                                 @Param("category") ExpenseCategory category);

    @Query("SELECT SUM(e.amount) FROM Expense e where e.user.id = :userId")
    BigDecimal getTotalAmountByUserId(@Param("userId") Long userId);
}
