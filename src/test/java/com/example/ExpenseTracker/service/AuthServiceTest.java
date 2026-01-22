package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.dto.RegisterDto;
import com.example.ExpenseTracker.entity.PasswordResetToken;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.entity.VerificationOtp;
import com.example.ExpenseTracker.repository.PasswordResetTokenRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import com.example.ExpenseTracker.repository.VerificationOtpRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepo;
    @Mock VerificationOtpRepository otpRepo;
    @Mock PasswordResetTokenRepository resetRepo;
    @Mock EmailService emailService;

    @InjectMocks AuthService authService;

    @Test
    void register_whenEmailExists_throws() {
        RegisterDto dto = new RegisterDto();
        dto.setName("Regin");
        dto.setEmail("a@test.com");
        dto.setPassword("Password@123");

        when(userRepo.existsByEmail("a@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");

        verify(userRepo, never()).save(any());
    }

    @Test
    void register_savesUser_andSendsOtp() {
        RegisterDto dto = new RegisterDto();
        dto.setName("Regin");
        dto.setEmail("a@test.com");
        dto.setPassword("Password@123");

        when(userRepo.existsByEmail("a@test.com")).thenReturn(false);

        authService.register(dto);

        verify(userRepo).save(argThat(u ->
                u.getEmail().equals("a@test.com")
                        && u.getName().equals("Regin")
                        && !u.isEnabled()
                        && u.getPassword() != null
                        && u.getPassword().startsWith("$2")
        ));

        // sendOtp deletes + saves otp + sends email
        verify(otpRepo).deleteByEmail("a@test.com");
        verify(otpRepo).save(any(VerificationOtp.class));
        verify(emailService).send(eq("a@test.com"), contains("OTP"), contains("OTP"));
    }

    @Test
    void verifyOtp_invalidOtp_throws() {
        VerificationOtp vo = new VerificationOtp();
        vo.setEmail("a@test.com");
        vo.setOtp("111111");
        vo.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(otpRepo.findTopByEmailOrderByIdDesc("a@test.com")).thenReturn(Optional.of(vo));

        assertThatThrownBy(() -> authService.verifyOtp("a@test.com", "222222"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid OTP");

        verify(userRepo, never()).save(any());
    }

    @Test
    void verifyOtp_expired_throws() {
        VerificationOtp vo = new VerificationOtp();
        vo.setEmail("a@test.com");
        vo.setOtp("111111");
        vo.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(otpRepo.findTopByEmailOrderByIdDesc("a@test.com")).thenReturn(Optional.of(vo));

        assertThatThrownBy(() -> authService.verifyOtp("a@test.com", "111111"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("OTP expired");
    }

    @Test
    void verifyOtp_success_enablesUser_andDeletesOtp() {
        VerificationOtp vo = new VerificationOtp();
        vo.setEmail("a@test.com");
        vo.setOtp("111111");
        vo.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        User user = new User();
        user.setEmail("a@test.com");
        user.setEnabled(false);

        when(otpRepo.findTopByEmailOrderByIdDesc("a@test.com")).thenReturn(Optional.of(vo));
        when(userRepo.findByEmail("a@test.com")).thenReturn(Optional.of(user));

        authService.verifyOtp("a@test.com", "111111");

        assertThat(user.isEnabled()).isTrue();
        verify(userRepo).save(user);
        verify(otpRepo).deleteByEmail("a@test.com");
    }

    @Test
    void login_whenNotEnabled_throws() {
        User u = new User();
        u.setEmail("a@test.com");
        u.setEnabled(false);
        u.setPassword("$2a$10$hashhashhashhashhashhashhashhashhashhashhash");

        when(userRepo.findByEmail("a@test.com")).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> authService.login("a@test.com", "Password@123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Verify email OTP first");
    }

    @Test
    void forgotPassword_createsToken_andSendsEmail() {
        User u = new User();
        u.setEmail("a@test.com");
        when(userRepo.findByEmail("a@test.com")).thenReturn(Optional.of(u));

        authService.forgotPassword("a@test.com", "http://localhost:8080");

        verify(resetRepo).deleteByEmail("a@test.com");
        verify(resetRepo).save(any(PasswordResetToken.class));
        verify(emailService).send(eq("a@test.com"), contains("Reset"), contains("/reset-password?token="));
    }

    @Test
    void resetPassword_valid_updatesPassword_andDeletesToken() {
        PasswordResetToken prt = new PasswordResetToken();
        prt.setEmail("a@test.com");
        prt.setToken("t");
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        User user = new User();
        user.setEmail("a@test.com");
        user.setPassword("$2a$old");

        when(resetRepo.findByToken("t")).thenReturn(Optional.of(prt));
        when(userRepo.findByEmail("a@test.com")).thenReturn(Optional.of(user));

        authService.resetPassword("t", "NewPass@123");

        assertThat(user.getPassword()).startsWith("$2"); // bcrypt
        verify(userRepo).save(user);
        verify(resetRepo).deleteByToken("t");
    }
}
