package com.example.ExpenseTracker.controller;

import com.example.ExpenseTracker.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository repo;

    public NotificationController(NotificationRepository repo) {
        this.repo = repo;
    }

    private Long uid(HttpSession session) {
        return (Long) session.getAttribute("USER_ID");
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        model.addAttribute("notifications", repo.findTop20ByUserIdOrderByIdDesc(uid(session)));
        return "notifications/list";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable Long id, HttpSession session) {
        var n = repo.findById(id).orElseThrow();
        if (!n.getUser().getId().equals(uid(session))) return "redirect:/notifications?forbidden";
        n.setReadFlag(true);
        repo.save(n);
        return "redirect:/notifications";
    }
}
