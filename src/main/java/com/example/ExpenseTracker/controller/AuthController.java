package com.example.ExpenseTracker.controller;

import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService authService;

    @Value("${app.base-url}")
    private String baseUrl;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterDto registerDto, BindingResult br, Model model) {
        if (br.hasErrors()) return "auth/register";
        try {
            authService.register(registerDto);
            model.addAttribute("email", registerDto.getEmail());
            model.addAttribute("otpDto", new OtpDto());
            return "auth/verify-otp";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam(required = false) String email, Model model) {
        OtpDto dto = new OtpDto();
        dto.setEmail(email);
        model.addAttribute("otpDto", dto);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@Valid @ModelAttribute OtpDto otpDto, BindingResult br, Model model) {
        if (br.hasErrors()) return "auth/verify-otp";
        try {
            authService.verifyOtp(otpDto.getEmail(), otpDto.getOtp());
            return "redirect:/login?verified";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/verify-otp";
        }
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginDto", new LoginDto());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginDto loginDto, BindingResult br,
                        HttpSession session, Model model) {
        if (br.hasErrors()) return "auth/login";
        try {
            User u = authService.login(loginDto.getEmail(), loginDto.getPassword());
            session.setAttribute("USER_ID", u.getId());
            session.setAttribute("ROLE", u.getRole());
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    @GetMapping("/forgot-password")
    public String forgotPage(Model model) {
        model.addAttribute("forgotPasswordDto", new ForgotPasswordDto());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgot(@Valid @ModelAttribute ForgotPasswordDto dto, BindingResult br, Model model) {
        if (br.hasErrors()) return "auth/forgot-password";
        try {
            authService.forgotPassword(dto.getEmail(), baseUrl);
            model.addAttribute("success", "Reset link sent to email");
            return "auth/forgot-password";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPage(@RequestParam String token, Model model) {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setToken(token);
        model.addAttribute("resetPasswordDto", dto);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String reset(@Valid @ModelAttribute ResetPasswordDto dto, BindingResult br, Model model) {
        if (br.hasErrors()) return "auth/reset-password";
        try {
            authService.resetPassword(dto.getToken(), dto.getNewPassword());
            return "redirect:/login?resetSuccess";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/reset-password";
        }
    }
}
