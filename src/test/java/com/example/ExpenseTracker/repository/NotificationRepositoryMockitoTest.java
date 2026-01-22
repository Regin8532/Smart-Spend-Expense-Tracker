package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entity.AppNotification;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationRepositoryMockitoTest {

    @Test
    void findTop20AndCountUnread() {
        NotificationRepository repo = mock(NotificationRepository.class);

        when(repo.findTop20ByUserIdOrderByIdDesc(1L)).thenReturn(List.of(new AppNotification()));
        when(repo.countByUserIdAndReadFlagFalse(1L)).thenReturn(3L);

        assertThat(repo.findTop20ByUserIdOrderByIdDesc(1L)).hasSize(1);
        assertThat(repo.countByUserIdAndReadFlagFalse(1L)).isEqualTo(3L);

        verify(repo).findTop20ByUserIdOrderByIdDesc(1L);
        verify(repo).countByUserIdAndReadFlagFalse(1L);
    }
}
