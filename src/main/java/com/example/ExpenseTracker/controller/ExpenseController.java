package com.example.ExpenseTracker.controller;

import com.example.ExpenseTracker.dto.ExpenseDto;
import com.example.ExpenseTracker.entity.Expense;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.repository.ExpenseRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import com.example.ExpenseTracker.service.ExpenseService;
import com.example.ExpenseTracker.service.ReportService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepo;
    private final UserRepository userRepo;
    private final ReportService reportService;

    public ExpenseController(ExpenseService expenseService, ExpenseRepository expenseRepo,
                             UserRepository userRepo, ReportService reportService) {
        this.expenseService = expenseService;
        this.expenseRepo = expenseRepo;
        this.userRepo = userRepo;
        this.reportService = reportService;
    }

    private Long uid(HttpSession session) {
        return (Long) session.getAttribute("USER_ID");
    }

    @GetMapping
    public String list(
            HttpSession session,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false, defaultValue = "dateDesc") String sort,
            Model model
    ) {
        List<Expense> list = expenseService.filter(uid(session), type, category, fromDate, toDate, minAmount, maxAmount, sort);
        model.addAttribute("expenses", list);

        // keep filters in form
        model.addAttribute("type", type);
        model.addAttribute("category", category);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("minAmount", minAmount);
        model.addAttribute("maxAmount", maxAmount);
        model.addAttribute("sort", sort);

        return "expenses/list";
    }

    @GetMapping("/new")
    public String createPage(Model model) {
        ExpenseDto dto = new ExpenseDto();
        dto.setDate(LocalDate.now());
        dto.setType("EXPENSE");
        model.addAttribute("expenseDto", dto);
        return "expenses/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute ExpenseDto expenseDto, BindingResult br,
                         HttpSession session, Model model) {
        if (br.hasErrors()) return "expenses/form";

        User user = userRepo.findById(uid(session)).orElseThrow();
        expenseService.save(user, expenseDto);
        return "redirect:/expenses?created";
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, HttpSession session, Model model) {
        Expense e = expenseRepo.findById(id).orElseThrow();
        if (!e.getUser().getId().equals(uid(session))) return "redirect:/expenses?forbidden";

        ExpenseDto dto = new ExpenseDto();
        dto.setTitle(e.getTitle());
        dto.setAmount(e.getAmount());
        dto.setCategory(e.getCategory());
        dto.setType(e.getType());
        dto.setDate(e.getDate());
        dto.setDescription(e.getDescription());

        model.addAttribute("expenseId", id);
        model.addAttribute("expenseDto", dto);
        return "expenses/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute ExpenseDto expenseDto, BindingResult br,
                         HttpSession session, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("expenseId", id);
            return "expenses/form";
        }

        Expense e = expenseRepo.findById(id).orElseThrow();
        if (!e.getUser().getId().equals(uid(session))) return "redirect:/expenses?forbidden";

        e.setTitle(expenseDto.getTitle());
        e.setAmount(expenseDto.getAmount());
        e.setCategory(expenseDto.getCategory());
        e.setType(expenseDto.getType());
        e.setDate(expenseDto.getDate());
        e.setDescription(expenseDto.getDescription());
        expenseRepo.save(e);

        return "redirect:/expenses?updated";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        Expense e = expenseRepo.findById(id).orElseThrow();
        if (!e.getUser().getId().equals(uid(session))) return "redirect:/expenses?forbidden";
        expenseRepo.deleteById(id);
        return "redirect:/expenses?deleted";
    }

    @GetMapping("/export/pdf")
    public void exportPdf(
            HttpSession session,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false, defaultValue = "dateDesc") String sort,
            jakarta.servlet.http.HttpServletResponse response
    ) throws Exception {
        var list = expenseService.filter(uid(session), type, category, fromDate, toDate, minAmount, maxAmount, sort);
        reportService.writePdf(response, list);
    }

    @GetMapping("/export/excel")
    public void exportExcel(
            HttpSession session,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false, defaultValue = "dateDesc") String sort,
            jakarta.servlet.http.HttpServletResponse response
    ) throws Exception {
        var list = expenseService.filter(uid(session), type, category, fromDate, toDate, minAmount, maxAmount, sort);
        reportService.writeExcel(response, list);
    }
}
