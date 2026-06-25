package com.AIExpense.ExpenseTracker.report.service;


import com.AIExpense.ExpenseTracker.budget.entity.Budget;
import com.AIExpense.ExpenseTracker.budget.repository.BudgetRepository;
import com.AIExpense.ExpenseTracker.common.dto.CachedList;
import com.AIExpense.ExpenseTracker.expense.entity.Expense;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.expense.repository.ExpenseRepository;
import com.AIExpense.ExpenseTracker.report.dto.*;
import com.AIExpense.ExpenseTracker.user.entity.User;
import com.AIExpense.ExpenseTracker.util.AuthenticationUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final AuthenticationUtils authenticationUtils;


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "monthlySummary", key = "#month + '-' + #year + '-' + @authenticationUtils.getCurrentUser().id")
    public MonthlySummaryResponse getMonthlySummary(int month, int year) {

        log.info("Getting monthly summary for month: {} year: {}", month, year);

        User user = authenticationUtils.getCurrentUser();

        BigDecimal totalSpent = expenseRepository.getTotalSpentByMonth(user.getId(), month, year);

        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        int totalTransactions = expenseRepository.countTransactionsByMonth(user.getId(), month, year);

        BigDecimal highestExpense = expenseRepository
                .getHighestExpenseByMonth(user.getId(), month, year);
        if (highestExpense == null) {
            highestExpense = BigDecimal.ZERO;
        }

        int totalDaysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        BigDecimal avgSpending = (totalTransactions > 0)
                ? totalSpent.divide(BigDecimal.valueOf(totalDaysInMonth), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;


        List<Object[]> categoryData = expenseRepository
                .getSpendingByCategoryForMonth(user.getId(), month, year);

        String highestSpendingCategory = categoryData.stream()
                .findFirst()
                .map(row -> ((ExpenseCategory) row[0]).name())
                .orElse("NO_EXPENSES");


        BigDecimal finalTotalSpent = totalSpent;
        List<CategoryBreakdown> categoryBreakdowns = categoryData.stream()
                .map(row ->
                        new CategoryBreakdown(
                                (ExpenseCategory) row[0],
                                (BigDecimal) row[1],
                                finalTotalSpent.compareTo(BigDecimal.ZERO) > 0
                                        ? ((BigDecimal) row[1]).divide(finalTotalSpent, 4, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                                        : 0.0
                        )).toList();


        return new MonthlySummaryResponse(
                month,
                year,
                totalSpent,
                totalTransactions,
                avgSpending,
                highestExpense,
                highestSpendingCategory,
                categoryBreakdowns

        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categorySummary", key = "#category + '-' + #month + '-' + #year + '-' + @authenticationUtils.getCurrentUser().id")
    public CategorySummaryResponse getCategorySummary(ExpenseCategory category, int month, int year) {

        log.info("Getting Category summary for: {} for month: {} year: {}",
                category, month, year);

        User user = authenticationUtils.getCurrentUser();

        List<Expense> expenses = expenseRepository
                .findByCategoryAndUserIdAndMonthAndYear(
                        category, user.getId(), month, year);

        if (expenses == null) expenses = List.of();

        BigDecimal totalSpent = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTransactions = expenses.size();

        Optional<Budget> budget = budgetRepository
                .findActiveBudget(user.getId(), category, month, year);

        BigDecimal budgetLimit = budget
                .map(Budget::getMonthlyLimit)
                .orElse(BigDecimal.ZERO);


        BigDecimal remainingAmount = budgetLimit.subtract(totalSpent);

        double utilizationPercentage = budgetLimit.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent
                .divide(budgetLimit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        String status = budget.isEmpty()
                ? "NO_BUDGET"
                : totalSpent.compareTo(budgetLimit) > 0
                ? "EXCEEDED"
                : "WITHIN_BUDGET";


        return new CategorySummaryResponse(
                category,
                totalSpent,
                budgetLimit,
                remainingAmount,
                utilizationPercentage,
                totalTransactions,
                status
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "topSpendingCategories", key = "#month + '-' + #year + '-' + #limit + '-' + @authenticationUtils.getCurrentUser().id")
    public List<TopSpendingCategoryResponse> getTopSpendingCategories(int month, int year, int limit) {

        log.info("Getting top {} spending categories for month: {} year: {}",
                limit, month, year);

        User user = authenticationUtils.getCurrentUser();

        List<Object[]> categoryData = expenseRepository
                .getSpendingByCategoryForMonth(user.getId(), month, year);

        if (categoryData.isEmpty()) {
            return List.of();
        }

        BigDecimal totalSpent = categoryData.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        AtomicInteger rankCounter = new AtomicInteger(1);

        return categoryData.stream()
                .limit(limit)
                .map(row -> {
                    ExpenseCategory category = (ExpenseCategory) row[0];

                    BigDecimal categorySpent = (BigDecimal) row[1];

                    double percentageOfTotal = categorySpent
                            .divide(totalSpent, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();

                    return new TopSpendingCategoryResponse(
                            rankCounter.getAndIncrement(), // rank: 1, 2, 3...
                            category,
                            categorySpent,
                            percentageOfTotal
                    );
                })
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "budgetRemaining", key = "#month + '-' + #year + '-' + @authenticationUtils.getCurrentUser().id")
    public List<BudgetRemainingResponse> getBudgetRemaining(int month, int year) {

        log.info("Getting Budget remaining for each category for month: {} year: {}",
                month, year);

        User user = authenticationUtils.getCurrentUser();

        List<Object[]> categoryData = expenseRepository
                .getSpendingByCategoryForMonth(user.getId(), month, year);

        List<Budget> budgetList = budgetRepository
                .findAllByUserIdAndMonthAndYear(user.getId(), month, year);

        if (categoryData.isEmpty() && budgetList.isEmpty()) {
            return List.of();
        }

        Map<ExpenseCategory, BigDecimal> budgetMap = budgetList.stream()
                .collect(Collectors.toMap(
                        Budget::getCategory,
                        Budget::getMonthlyLimit
                ));

        Map<ExpenseCategory, BigDecimal> expenseMap = categoryData.stream()
                .collect(Collectors.toMap(
                        row -> (ExpenseCategory) row[0],
                        row -> (BigDecimal) row[1]
                ));

        // Ensures categories with budget but no expenses are included
        Set<ExpenseCategory> allCategories = new LinkedHashSet<>();
        allCategories.addAll(expenseMap.keySet());
        allCategories.addAll(budgetMap.keySet());


        return allCategories.stream()
                .map(category -> {
                    BigDecimal totalSpent = expenseMap
                            .getOrDefault(category, BigDecimal.ZERO);

                    BigDecimal budgetLimit = budgetMap
                            .getOrDefault(category, BigDecimal.ZERO);

                    BigDecimal remainingAmount = budgetLimit.subtract(totalSpent);

                    double utilizationPercentage = budgetLimit
                            .compareTo(BigDecimal.ZERO) > 0
                            ? totalSpent
                            .divide(budgetLimit, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                            : 0.0;

                    String status = budgetLimit.compareTo(BigDecimal.ZERO) == 0
                            ? "NO_BUDGET"
                            : totalSpent.compareTo(budgetLimit) > 0
                            ? "EXCEEDED"
                            : "WITHIN_BUDGET";

                    return new BudgetRemainingResponse(
                            category,
                            budgetLimit,
                            totalSpent,
                            remainingAmount,
                            utilizationPercentage,
                            status
                    );
                })
                .sorted(Comparator.comparing(BudgetRemainingResponse::utilizationPercentage)
                        .reversed()) // highest utilization first
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "monthlyTrend", key = "#months + '-' + @authenticationUtils.getCurrentUser().id")
    public List<MonthlyTrendResponse> getMonthlyTrend(int months) {

        log.info("Getting monthly trend for last {} months", months);

        User user = authenticationUtils.getCurrentUser();

        // Calculate start date based on number of months requested
        LocalDate startDate = LocalDate.now()
                .minusMonths(months)
                .withDayOfMonth(1);

        List<Object[]> trendData = expenseRepository
                .getMonthlyTrend(user.getId(), startDate);

        if (trendData.isEmpty()) {
            return List.of();
        }

        List<MonthlyTrendResponse> trendList = new ArrayList<>();

        for (int i = 0; i < trendData.size(); i++) {
            Object[] row = trendData.get(i);

            int month = ((Number) row[0]).intValue();
            int year = ((Number) row[1]).intValue();
            BigDecimal totalSpent = row[2] != null
                    ? (BigDecimal) row[2]
                    : BigDecimal.ZERO;

            int totalTransactions = ((Number) row[3]).intValue();

            String monthName = Month.of(month).getDisplayName(
                    TextStyle.FULL, Locale.ENGLISH);

            BigDecimal changeFromLastMonth;
            double changePercentage;

            if (i == 0) {
                changeFromLastMonth = BigDecimal.ZERO;
                changePercentage = 0.0;
            } else {
                BigDecimal previousMonthSpent = trendList.get(i - 1).totalSpent();
                changeFromLastMonth = totalSpent.subtract(previousMonthSpent);

                changePercentage = previousMonthSpent.compareTo(BigDecimal.ZERO) > 0
                        ? changeFromLastMonth
                        .divide(previousMonthSpent, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue()
                        : 0.0;
            }
            trendList.add(new MonthlyTrendResponse(
                    month,
                    year,
                    monthName,
                    totalSpent,
                    totalTransactions, // totalTransactions placeholder
                    changeFromLastMonth,
                    changePercentage
            ));

        }

        return trendList;

    }


}
