package com.example.ExpenseTracker.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String path = req.getRequestURI();

        if (path.startsWith("/login") || path.startsWith("/register") || path.startsWith("/verify-otp")
                || path.startsWith("/forgot-password") || path.startsWith("/reset-password")
                || path.startsWith("/css") || path.startsWith("/js") || path.startsWith("/images")
                || path.startsWith("/ws")) {
            return true;
        }

        Object userId = req.getSession().getAttribute("USER_ID");
        if (userId == null) {
            res.sendRedirect("/login");
            return false;
        }
        return true;
    }
}
