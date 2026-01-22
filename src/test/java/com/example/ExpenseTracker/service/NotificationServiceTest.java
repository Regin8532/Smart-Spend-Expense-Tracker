package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.entity.AppNotification;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository repo;
    @Mock SimpMessagingTemplate messaging;

    @InjectMocks NotificationService service;

    @Test
    void notifyUser_savesNotification_andSendsWebsocket() {
        User u = new User();
        u.setId(10L);

        service.notifyUser(u, "hello");

        verify(repo).save(any(AppNotification.class));
        verify(messaging).convertAndSend("/topic/alerts/10", "hello");
    }
}
