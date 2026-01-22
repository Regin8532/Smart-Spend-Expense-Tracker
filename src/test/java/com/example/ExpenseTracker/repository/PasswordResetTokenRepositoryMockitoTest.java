package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entity.PasswordResetToken;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PasswordResetTokenRepositoryMockitoTest {

    @Test
    void findByToken_deleteByEmail_deleteByToken() {
        PasswordResetTokenRepository repo = mock(PasswordResetTokenRepository.class);

        PasswordResetToken t = new PasswordResetToken();
        t.setToken("abc");

        when(repo.findByToken("abc")).thenReturn(Optional.of(t));

        assertThat(repo.findByToken("abc")).isPresent();

        repo.deleteByEmail("a@test.com");
        repo.deleteByToken("abc");

        verify(repo).findByToken("abc");
        verify(repo).deleteByEmail("a@test.com");
        verify(repo).deleteByToken("abc");
    }
}
