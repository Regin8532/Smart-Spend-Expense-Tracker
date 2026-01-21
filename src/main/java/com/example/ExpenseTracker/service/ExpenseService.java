package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.dto.ExpenseDto;
import com.example.ExpenseTracker.entity.Budget;
import com.example.ExpenseTracker.entity.Expense;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepo;
    private final BudgetService budgetService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public ExpenseService(ExpenseRepository expenseRepo, BudgetService budgetService,
                          NotificationService notificationService, EmailService emailService) {
        this.expenseRepo = expenseRepo;
        this.budgetService = budgetService;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    public Expense save(User user, ExpenseDto dto) {
        Expense e = new Expense();
        e.setUser(user);
        e.setTitle(dto.getTitle());
        e.setAmount(dto.getAmount());
        e.setCategory(dto.getCategory());
        e.setType(dto.getType());
        e.setDate(dto.getDate());
        e.setDescription(dto.getDescription());

        Expense saved = expenseRepo.save(e);

        if ("EXPENSE".equalsIgnoreCase(dto.getType())) {
            checkBudgetAlerts(user, dto.getCategory(), dto.getDate());
        }
        return saved;
    }

    public List<Expense> filter(Long userId, String type, String category,
                                LocalDate fromDate, LocalDate toDate,
                                BigDecimal minAmount, BigDecimal maxAmount, String sort) {
        return expenseRepo.filter(userId, emptyToNull(type), emptyToNull(category),
                fromDate, toDate, minAmount, maxAmount, emptyToNull(sort));
    }

    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private void checkBudgetAlerts(User user, String category, LocalDate expenseDate) {
        String month = YearMonth.from(expenseDate).toString(); // 2026-01
        Budget b = budgetService.getBudget(user.getId(), month, category);
        if (b == null) return;

        LocalDate start = BudgetService.monthStart(month);
        LocalDate end = BudgetService.monthEnd(month);

        BigDecimal spent = expenseRepo.sumCategoryExpenseInRange(user.getId(), category, start, end);

        if (BudgetService.isExceeded(spent, b.getLimitAmount())) {
            String msg = "Budget exceeded for " + category + " (" + month + "). Spent: " + spent + " / Limit: " + b.getLimitAmount();
            notificationService.notifyUser(user, msg);
            emailService.send(user.getEmail(), "SmartSpend Budget Exceeded", msg);
        } else if (BudgetService.isNear(spent, b.getLimitAmount())) {
            String msg = "Budget nearing limit for " + category + " (" + month + "). Spent: " + spent + " / Limit: " + b.getLimitAmount();
            notificationService.notifyUser(user, msg);
            emailService.send(user.getEmail(), "SmartSpend Budget Alert", msg);
        }
    }
}
