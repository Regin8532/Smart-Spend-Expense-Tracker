package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entity.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findTop20ByUserIdOrderByIdDesc(Long userId);
    long countByUserIdAndReadFlagFalse(Long userId);
}
