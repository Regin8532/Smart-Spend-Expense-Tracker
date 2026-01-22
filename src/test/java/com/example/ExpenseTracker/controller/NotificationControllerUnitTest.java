package com.example.ExpenseTracker.controller;

import com.example.ExpenseTracker.entity.AppNotification;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationControllerUnitTest {

    @Mock NotificationRepository repo;
    @Mock HttpSession session;
    @Mock Model model;

    NotificationController controller;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        controller = new NotificationController(repo);
        when(session.getAttribute("USER_ID")).thenReturn(1L);
    }

    @Test
    void list_addsNotifications_returnsView() {
        when(repo.findTop20ByUserIdOrderByIdDesc(1L)).thenReturn(List.of(new AppNotification()));

        String view = controller.list(session, model);

        assertThat(view).isEqualTo("notifications/list");
        verify(model).addAttribute(eq("notifications"), anyList());
    }

    @Test
    void markRead_notOwner_redirectsForbidden() {
        AppNotification n = new AppNotification();
        User other = new User();
        other.setId(99L);
        n.setUser(other);

        when(repo.findById(5L)).thenReturn(Optional.of(n));

        String view = controller.markRead(5L, session);

        assertThat(view).isEqualTo("redirect:/notifications?forbidden");
        verify(repo, never()).save(any());
    }

    @Test
    void markRead_owner_setsReadTrue_saves_redirects() {
        AppNotification n = new AppNotification();
        User owner = new User();
        owner.setId(1L);
        n.setUser(owner);
        n.setReadFlag(false);

        when(repo.findById(5L)).thenReturn(Optional.of(n));

        String view = controller.markRead(5L, session);

        assertThat(view).isEqualTo("redirect:/notifications");
        verify(repo).save(n);
        assertThat(n.isReadFlag()).isTrue();
    }
}
