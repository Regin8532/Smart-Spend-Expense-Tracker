package com.example.ExpenseTracker.controller;

import com.example.ExpenseTracker.dto.BudgetDto;
import com.example.ExpenseTracker.entity.Budget;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.repository.BudgetRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetRepository budgetRepo;
    private final UserRepository userRepo;

    public BudgetController(BudgetRepository budgetRepo, UserRepository userRepo) {
        this.budgetRepo = budgetRepo;
        this.userRepo = userRepo;
    }

    private Long uid(HttpSession session) {
        return (Long) session.getAttribute("USER_ID");
    }

    @GetMapping
    public String page(@RequestParam(required = false) String month,
                       @RequestParam(required = false) Long editId,
                       HttpSession session, Model model) {

        if (month == null || month.isBlank()) month = java.time.YearMonth.now().toString();

        var list = budgetRepo.findByUserIdAndMonth(uid(session), month);

        BudgetDto dto = new BudgetDto();
        dto.setMonth(month);

        if (editId != null) {
            Budget b = budgetRepo.findById(editId).orElseThrow();
            if (!b.getUser().getId().equals(uid(session))) return "redirect:/budgets?forbidden";

            dto.setMonth(b.getMonth());
            dto.setCategory(b.getCategory());
            dto.setLimitAmount(b.getLimitAmount());

            model.addAttribute("editId", editId);
        }

        model.addAttribute("month", month);
        model.addAttribute("budgets", list);
        model.addAttribute("budgetDto", dto);
        return "budgets/list";
    }


    @PostMapping
    public String save(@Valid @ModelAttribute BudgetDto budgetDto, BindingResult br, HttpSession session, Model model) {
        if (br.hasErrors()) return "redirect:/budgets?month=" + budgetDto.getMonth();

        User user = userRepo.findById(uid(session)).orElseThrow();

        Budget b = budgetRepo.findByUserIdAndMonthAndCategory(user.getId(), budgetDto.getMonth(), budgetDto.getCategory())
                .orElseGet(Budget::new);

        b.setUser(user);
        b.setMonth(budgetDto.getMonth());
        b.setCategory(budgetDto.getCategory());
        b.setLimitAmount(budgetDto.getLimitAmount());
        budgetRepo.save(b);

        return "redirect:/budgets?month=" + budgetDto.getMonth() + "&saved";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        Budget b = budgetRepo.findById(id).orElseThrow();
        if (!b.getUser().getId().equals(uid(session))) return "redirect:/budgets?forbidden";
        String month = b.getMonth();
        budgetRepo.deleteById(id);
        return "redirect:/budgets?month=" + month + "&deleted";
    }
}
