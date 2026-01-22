package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entity.VerificationOtp;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VerificationOtpRepositoryMockitoTest {

    @Test
    void findTopByEmailOrderByIdDesc_and_deleteByEmail() {
        VerificationOtpRepository repo = mock(VerificationOtpRepository.class);

        VerificationOtp otp = new VerificationOtp();
        otp.setEmail("a@test.com");
        otp.setOtp("123456");
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(repo.findTopByEmailOrderByIdDesc("a@test.com")).thenReturn(Optional.of(otp));

        assertThat(repo.findTopByEmailOrderByIdDesc("a@test.com")).isPresent();

        repo.deleteByEmail("a@test.com");

        verify(repo).findTopByEmailOrderByIdDesc("a@test.com");
        verify(repo).deleteByEmail("a@test.com");
    }
}
