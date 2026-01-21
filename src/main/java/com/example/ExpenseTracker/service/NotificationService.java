package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.entity.AppNotification;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final NotificationRepository repo;
    private final SimpMessagingTemplate messaging;

    public NotificationService(NotificationRepository repo, SimpMessagingTemplate messaging) {
        this.repo = repo;
        this.messaging = messaging;
    }

    public void notifyUser(User user, String message) {
        AppNotification n = new AppNotification();
        n.setUser(user);
        n.setMessage(message);
        repo.save(n);

        messaging.convertAndSend("/topic/alerts/" + user.getId(), message);
    }
}
