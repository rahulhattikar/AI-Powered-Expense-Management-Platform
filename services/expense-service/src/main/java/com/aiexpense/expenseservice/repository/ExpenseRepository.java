package com.aiexpense.expenseservice.repository;

import com.aiexpense.expenseservice.entity.Expense;
import com.aiexpense.expenseservice.entity.ExpenseCategory;
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


    @Query("SELECT e FROM Expense e WHERE e.id = :id AND e.isDeleted = false")
    Optional<Expense> findByIdAndNotDeleted(@Param("id") Long id);

    Page<Expense> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    @Query("""
            SELECT e FROM Expense e
            WHERE e.userId = :userId
            AND e.category = :category
            AND e.isDeleted = false
            """)
    Page<Expense> findByUserIdAndCategory(
            @Param("userId") Long userId,
            @Param("category") ExpenseCategory category,
            Pageable pageable
    );


    @Query("""
            SELECT e FROM Expense e
            WHERE e.userId = :userId
            AND e.expenseDate BETWEEN :startDate AND :endDate
            AND e.isDeleted = false
            """)
    Page<Expense> findByDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("""
            SELECT e FROM Expense e
            WHERE e.userId = :userId
            AND e.amount BETWEEN :minAmount AND :maxAmount
            AND e.isDeleted = false
            """)
    Page<Expense> findByAmountRange(
            @Param("userId") Long userId,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable
    );


    @Query("""
            SELECT e FROM Expense e
            WHERE e.userId = :userId
            AND e.category = :category
            AND e.expenseDate BETWEEN :startDate AND :endDate
            AND e.amount >= :minAmount
            AND e.isDeleted = false
            ORDER BY e.expenseDate DESC
            """)
    Page<Expense> findWithMultipleFilters(
            @Param("userId") Long userId,
            @Param("category") ExpenseCategory category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minAmount") BigDecimal minAmount,
            Pageable pageable
    );


    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.userId = :userId AND e.isDeleted = false
            """)
    BigDecimal getTotalExpensesByUser(@Param("userId") Long userId);


    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.userId = :userId
            AND e.category = :category
            AND e.isDeleted = false
            """)
    BigDecimal getTotalByCategory(
            @Param("userId") Long userId,
            @Param("category") ExpenseCategory category
    );


    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.userId = :userId
            AND e.category = :category
            AND MONTH(e.expenseDate) = :month
            AND e.isDeleted = false
            """)
    BigDecimal getTotalByCategoryAndMonth(
            @Param("userId") Long userId,
            @Param("category") ExpenseCategory category,
            @Param("month") int month
    );


    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.userId = :userId
    AND MONTH(e.expenseDate) = :month
    AND YEAR(e.expenseDate) = :year
    AND e.isDeleted = false
    """)
    BigDecimal getTotalSpentByMonth(
            @Param("userId") Long userId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    @Query("""
    SELECT MAX(e.amount)
    FROM Expense e
    WHERE e.userId = :userId
    AND MONTH(e.expenseDate) = :month
    AND YEAR(e.expenseDate) = :year
    AND e.isDeleted = false
    """)
    BigDecimal getHighestExpenseByMonth(
            @Param("userId") Long userId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    @Query("""
    SELECT e.category, SUM(e.amount), COUNT(e.category)
    FROM Expense e
    WHERE e.userId = :userId
    AND MONTH(e.expenseDate) = :month
    AND YEAR(e.expenseDate) = :year
    AND e.isDeleted = false
    GROUP BY e.category
    """)
    List<Object[]> getSpendingByCategoryForMonth(
            @Param("userId") Long userId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );


    @Query("""
    SELECT e FROM Expense e
    WHERE e.userId = :userId
    AND e.category = :category
    AND MONTH(e.expenseDate) = :month
    AND YEAR(e.expenseDate) = :year
    AND e.isDeleted = false
    ORDER BY e.expenseDate DESC
    """)
    List<Expense> findByCategoryAndUserIdAndMonthAndYear(
            @Param("userId") Long userId,
            @Param("category") ExpenseCategory category,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    @Query("""
    SELECT e.expenseDate, SUM(e.amount)
    FROM Expense e
    WHERE e.userId = :userId
    AND e.expenseDate >= :startDate
    AND e.isDeleted = false
    GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate)
    ORDER BY e.expenseDate DESC
    """)
    List<Object[]> getMonthlyTrend(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate
    );

    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.userId = :userId
    AND e.category = :category
    AND e.isDeleted = false
    """)
    BigDecimal getTotalSpendingByCategory(
            @Param("userId") Long userId,
            @Param("category") ExpenseCategory category
    );


    @Query("""
            SELECT COUNT(e)
            FROM Expense e
            WHERE e.userId = :userId
            AND MONTH(e.expenseDate) = :month
            AND YEAR(e.expenseDate) = :year
            AND e.isDeleted = false
            """
            )
    int countTransactionsByMonth(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year
    );

}
