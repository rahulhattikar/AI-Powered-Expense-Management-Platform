package com.AIExpense.ExpenseTracker.expense.repository;

import com.AIExpense.ExpenseTracker.expense.entity.Expense;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    Page<Expense> findByUserId(Long userId, Pageable pageable);

    List<Expense> findByCategoryAndUserId(ExpenseCategory category, Long userId);

    List<Expense> findByUserIdAndExpenseDateBetween(Long userId,
                                                    LocalDate startDate,
                                                    LocalDate endDate);

    @Query("SELECT e FROM Expense e where e.category=:category " +
            "AND e.user.id = :userId " +
            "AND MONTH(e.expenseDate) = :month " +
            "AND YEAR(e.expenseDate) = :year")
    List<Expense> findByCategoryAndUserIdAndMonthAndYear(
            @Param("category") ExpenseCategory category,
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year);

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

    // Monthly total spending
    @Query("SELECT SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND MONTH(e.expenseDate) = :month " +
            "AND YEAR(e.expenseDate) = :year")
    BigDecimal getTotalSpentByMonth(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year
    );

    // Count transactions per month
    @Query("SELECT COUNT(e) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND MONTH(e.expenseDate) = :month " +
            "AND YEAR(e.expenseDate) = :year")
    int countTransactionsByMonth(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year
    );

    // Spending per category for a month
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND MONTH(e.expenseDate) = :month " +
            "AND YEAR(e.expenseDate) = :year " +
            "GROUP BY e.category " +
            "ORDER BY SUM(e.amount) DESC")
    List<Object[]> getSpendingByCategoryForMonth(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year
    );

    // Highest single expense in a month
    @Query("SELECT MAX(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND MONTH(e.expenseDate) = :month " +
            "AND YEAR(e.expenseDate) = :year")
    BigDecimal getHighestExpenseByMonth(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year
    );

    // Monthly trend - last N months
    @Query("SELECT MONTH(e.expenseDate), YEAR(e.expenseDate), " +
            "SUM(e.amount), COUNT(e) " +
            "FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND e.expenseDate >= :startDate " +
            "GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate) " +
            "ORDER BY YEAR(e.expenseDate) ASC, MONTH(e.expenseDate) ASC")
    List<Object[]> getMonthlyTrend(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate
    );

}
