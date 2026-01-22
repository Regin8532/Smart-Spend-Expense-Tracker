package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserRepositoryMockitoTest {

    @Test
    void existsByEmail_and_findByEmail() {
        UserRepository repo = mock(UserRepository.class);

        User u = new User();
        u.setId(1L);
        u.setEmail("regin@test.com");

        when(repo.existsByEmail("regin@test.com")).thenReturn(true);
        when(repo.findByEmail("regin@test.com")).thenReturn(Optional.of(u));

        assertThat(repo.existsByEmail("regin@test.com")).isTrue();
        assertThat(repo.findByEmail("regin@test.com")).isPresent();

        verify(repo).existsByEmail("regin@test.com");
        verify(repo).findByEmail("regin@test.com");
    }
}
