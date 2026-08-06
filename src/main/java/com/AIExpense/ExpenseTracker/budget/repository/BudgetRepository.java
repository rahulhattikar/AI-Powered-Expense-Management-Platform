package com.AIExpense.ExpenseTracker.budget.repository;

import com.AIExpense.ExpenseTracker.budget.entity.Budget;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("SELECT b FROM Budget b WHERE b.userId = :userId " +
            "AND b.category = :category")
    List<Budget> findByUserIdAndCategory(
            @Param("userId") Long userId,
            @Param("category") ExpenseCategory category
    );

    @Query("SELECT b FROM Budget b WHERE b.id = :id " +
            "AND b.userId = :userId")
    Optional<Budget> findByIdAndUserId(
            @Param("id") Long budgetId,
            @Param("userId") Long userId);

    @Query("SELECT b FROM Budget b WHERE b.userId = :userId " +
            "AND b.month = :month " +
            "AND b.year = :year")
    List<Budget> findAllByUserIdAndMonthAndYear(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("SELECT b FROM Budget b WHERE b.userId = :userId " +
            "AND b.category = :category " +
            "AND b.month = :month " +
            "AND b.year = :year")
    List<Budget> findByUserIdAndCategoryAndMonthAndYear(
            @Param("userId") Long userId,
            @Param("category") ExpenseCategory category,
            @Param("month") int month,
            @Param("year") int year);


    Page<Budget> findAllByUserId(Long userId , Pageable pageable);

    boolean existsByUserIdAndCategoryAndMonthAndYear(
            Long userId,
            ExpenseCategory category,
            int month,
            int year
    );

    @Query("SELECT b FROM Budget b WHERE b.userId = :userId " +
            "AND b.category = :category " +
            "AND b.month = :month " +
            "AND b.year = :year")
    Optional<Budget> findActiveBudget(
            @Param("userId") Long userId,
            @Param("category") ExpenseCategory category,
            @Param("month") int month,
            @Param("year") int year
    );


}
