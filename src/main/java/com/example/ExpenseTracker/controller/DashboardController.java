package com.example.ExpenseTracker.controller;

import com.example.ExpenseTracker.repository.ExpenseRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class DashboardController {

    private final ExpenseRepository expenseRepo;

    public DashboardController(ExpenseRepository expenseRepo) {
        this.expenseRepo = expenseRepo;
    }

    private Long uid(HttpSession session) {
        return (Long) session.getAttribute("USER_ID");
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(HttpSession session, Model model) {
        YearMonth ym = YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal income = expenseRepo.sumByTypeInRange(uid(session), "INCOME", start, end);
        BigDecimal expense = expenseRepo.sumByTypeInRange(uid(session), "EXPENSE", start, end);

        String[] categories = {"Food", "Travel", "Shopping", "Bills", "Rent", "Health", "Other"};
        Map<String, BigDecimal> cat = new LinkedHashMap<>();
        for (String c : categories) {
            cat.put(c, expenseRepo.sumCategoryExpenseInRange(uid(session), c, start, end));
        }

        model.addAttribute("month", ym.toString());
        model.addAttribute("income", income);
        model.addAttribute("expense", expense);
        model.addAttribute("catMap", cat);
        return "dashboard";
    }
}
