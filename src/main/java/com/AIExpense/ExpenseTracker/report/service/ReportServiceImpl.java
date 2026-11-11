package com.AIExpense.ExpenseTracker.report.service;


import com.AIExpense.ExpenseTracker.budget.entity.Budget;
import com.AIExpense.ExpenseTracker.budget.repository.BudgetRepository;
import com.AIExpense.ExpenseTracker.client.ExpenseServiceClient;

import com.AIExpense.ExpenseTracker.dto.ExpenseResponse;
import com.AIExpense.ExpenseTracker.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.report.dto.*;

import com.AIExpense.ExpenseTracker.util.*;
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

    private final ExpenseServiceClient expenseServiceClient;
    private final BudgetRepository budgetRepository;
    private final AuthenticationUtils authenticationUtils;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "monthlySummary",
            key = "#month + '-' + #year + '-' + @authenticationUtils.getCurrentUserId()",
            unless = "#result.totalTransactions == 0")
    public MonthlySummaryResponse getMonthlySummary(int month, int year) {

        log.info("Getting monthly summary for month: {} year: {}", month, year);

        Long userId = authenticationUtils.getCurrentUserId();
        String token  = authenticationUtils.getTokenFromRequest();

       MonthlySummaryResponse monthlySummaryResponse =  expenseServiceClient.getMonthlySummary(
                month, year , "Bearer " + token);

       BigDecimal totalSpent = monthlySummaryResponse.getTotalSpent() != null
                ? monthlySummaryResponse.getTotalSpent()
                : BigDecimal.ZERO;


        int totalTransactions = monthlySummaryResponse.getTotalTransactions();


        int totalDaysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        BigDecimal averagePerDay = (totalTransactions > 0)
                ? totalSpent.divide(BigDecimal.valueOf(totalDaysInMonth), 2,
                RoundingMode.HALF_UP)
                : BigDecimal.ZERO;


         BigDecimal highestExpense = monthlySummaryResponse.getHighestExpense();

        return new MonthlySummaryResponse(
                userId,
                month,
                year,
                totalSpent,
                totalTransactions,
                averagePerDay,
                highestExpense

        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categorySummary",
            key = "#category + '-' + #month + '-' + #year + '-' + @authenticationUtils.getCurrentUserId()",
            unless = "#result.totalSpent == 0")
    public CategorySummaryResponse getCategorySummary(ExpenseCategory category, int month, int year) {

        log.info("Getting Category summary for: {} for month: {} year: {}",
                category, month, year);

        Long userId = authenticationUtils.getCurrentUserId();
        String token  = authenticationUtils.getTokenFromRequest();

        List<ExpenseResponse> expenseResponses = expenseServiceClient
                .getExpensesByMonthAndCategory(
                        category.name(),
                        month,
                        year,
                        "Bearer " + token);

        List<ExpenseResponse> expenses = expenseResponses != null
                ? expenseResponses
                : List.of();


        BigDecimal totalSpent = expenses.stream()
                .map(ExpenseResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTransactions = expenses.size();

        Optional<Budget> budget = budgetRepository
                .findActiveBudget(userId, category, month, year);

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
    @Cacheable(value = "topSpendingCategories",
            key = "#month + '-' + #year + '-' + #limit + '-' + @authenticationUtils.getCurrentUserId()",
            unless = "#result.isEmpty()")
    public List<TopSpendingCategoryResponse> getTopSpendingCategories(int month, int year, int limit) {

        log.info("Getting top {} spending categories for month: {} year: {}",
                limit, month, year);


        String token  = authenticationUtils.getTokenFromRequest();

        List<CategorySpendingResponse> categoryResponses = expenseServiceClient
                .getCategoryBreakdown(
                        month,
                        year,
                        "Bearer " + token);

        List<Object[]> categoryData = categoryResponses != null
                ? categoryResponses.stream()
                .map(response -> new Object[]{
                        ExpenseCategory.valueOf(response.getCategory()),
                        response.getAmount()
                })
                .toList()
                : List.of();

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
                            rankCounter.getAndIncrement(),
                            category,
                            categorySpent,
                            percentageOfTotal
                    );
                })
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "budgetRemaining",
               key = "#month + '-' + #year + '-' + @authenticationUtils.getCurrentUserId()",
               unless = "#result.remainingAmount == 0")
    public List<BudgetRemainingResponse> getBudgetRemaining(int month, int year) {

        log.info("Getting Budget remaining for each category for month: {} year: {}",
                month, year);

        Long userId = authenticationUtils.getCurrentUserId();
        String token  = authenticationUtils.getTokenFromRequest();

        List<CategorySpendingResponse> categoryResponses = expenseServiceClient
                .getCategoryBreakdown(
                        month,
                        year,
                        "Bearer " + token);

        List<Object[]> categoryData = categoryResponses != null
                ? categoryResponses.stream()
                .map(response -> new Object[]{
                        ExpenseCategory.valueOf(response.getCategory()),
                        response.getAmount()
                })
                .toList()
                : List.of();

        List<Budget> budgetList = budgetRepository
                .findAllByUserIdAndMonthAndYear(userId, month, year);

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
    @Cacheable(value = "monthlyTrend",
            key = "#months + '-' + @authenticationUtils.getCurrentUserId()",
            unless = "#result.isEmpty()")
    public List<MonthlyTrendResponse> getMonthlyTrend(int months) {

        log.info("Getting monthly trend for last {} months", months);

        String token  = authenticationUtils.getTokenFromRequest();

        // Calculate start date based on number of months requested
        LocalDate startDate = LocalDate.now()
                .minusMonths(months)
                .withDayOfMonth(1);

        List<MonthlyTrendResponse> trendResponses = expenseServiceClient
                .getMonthlyTrend(
                        startDate.toString(),
                        "Bearer " + token);

        if (trendResponses == null || trendResponses.isEmpty()) {
            return List.of();
        }



        List<MonthlyTrendResponse> trendList = new ArrayList<>();

        for (int i = 0; i < trendResponses.size(); i++) {

            MonthlyTrendResponse response = trendResponses.get(i);


            BigDecimal totalSpent = response.getTotalSpent() != null
                    ? response.getTotalSpent()
                    : BigDecimal.ZERO;



            int totalTransactions = response.getTotalTransactions() != -1
                    ? response.getTotalTransactions()
                    : 0;


            String monthName = response.getMonthName() != null
                    ? response.getMonthName()
                    : Month.of(response.getMonth()).getDisplayName(
                    TextStyle.FULL, Locale.ENGLISH);

            BigDecimal changeFromLastMonth;
            double changePercentage;

            if (i == 0) {
                changeFromLastMonth = BigDecimal.ZERO;
                changePercentage = 0.0;
            } else {
                BigDecimal previousMonthSpent = trendList.get(i - 1).getTotalSpent();
                changeFromLastMonth = totalSpent.subtract(previousMonthSpent);

                changePercentage = previousMonthSpent.compareTo(BigDecimal.ZERO) > 0
                        ? changeFromLastMonth
                        .divide(previousMonthSpent, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue()
                        : 0.0;
            }

            trendList.add(new MonthlyTrendResponse(
                    response.getMonth(),
                    response.getYear(),
                    monthName,
                    totalSpent,
                    totalTransactions,
                    changeFromLastMonth,
                    changePercentage
            ));
        }

        return trendList;

    }


}
