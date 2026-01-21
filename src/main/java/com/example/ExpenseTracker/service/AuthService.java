package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.dto.RegisterDto;
import com.example.ExpenseTracker.entity.PasswordResetToken;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.entity.VerificationOtp;
import com.example.ExpenseTracker.repository.PasswordResetTokenRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import com.example.ExpenseTracker.repository.VerificationOtpRepository;
import com.example.ExpenseTracker.util.OtpUtil;
import com.example.ExpenseTracker.util.TokenUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final VerificationOtpRepository otpRepo;
    private final PasswordResetTokenRepository resetRepo;
    private final EmailService emailService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepo,
                       VerificationOtpRepository otpRepo,
                       PasswordResetTokenRepository resetRepo,
                       EmailService emailService) {
        this.userRepo = userRepo;
        this.otpRepo = otpRepo;
        this.resetRepo = resetRepo;
        this.emailService = emailService;
    }

    @Transactional
    public void register(RegisterDto dto) {
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User u = new User();
        u.setName(dto.getName());
        u.setEmail(dto.getEmail());
        u.setPassword(encoder.encode(dto.getPassword()));
        u.setEnabled(false);
        userRepo.save(u);

        sendOtp(dto.getEmail());
    }

    @Transactional
    public void sendOtp(String email) {
        otpRepo.deleteByEmail(email);

        String otp = OtpUtil.generate6DigitOtp();
        VerificationOtp vo = new VerificationOtp();
        vo.setEmail(email);
        vo.setOtp(otp);
        vo.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepo.save(vo);

        emailService.send(email, "SmartSpend OTP Verification",
                "Your SmartSpend OTP is: " + otp + "\nIt expires in 10 minutes.");
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        VerificationOtp vo = otpRepo.findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (LocalDateTime.now().isAfter(vo.getExpiresAt())) {
            throw new RuntimeException("OTP expired");
        }
        if (!vo.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        u.setEnabled(true);
        userRepo.save(u);
        otpRepo.deleteByEmail(email);
    }

    public User login(String email, String rawPassword) {
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!u.isEnabled()) throw new RuntimeException("Verify email OTP first");
        if (!encoder.matches(rawPassword, u.getPassword())) throw new RuntimeException("Invalid credentials");

        return u;
    }
    @Transactional
    public void forgotPassword(String email, String baseUrl) {
        userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not registered"));

        resetRepo.deleteByEmail(email);

        String token = TokenUtil.generateToken();
        PasswordResetToken prt = new PasswordResetToken();
        prt.setEmail(email);
        prt.setToken(token);
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        resetRepo.save(prt);

        String link = baseUrl + "/reset-password?token=" + token;

        emailService.send(email, "SmartSpend Password Reset",
                "Click the link to reset password (valid 30 min):\n" + link);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = resetRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (LocalDateTime.now().isAfter(prt.getExpiresAt())) {
            throw new RuntimeException("Token expired");
        }

        User user = userRepo.findByEmail(prt.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);

        resetRepo.deleteByToken(token);
    }

}
