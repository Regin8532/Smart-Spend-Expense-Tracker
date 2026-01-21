package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entity.VerificationOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface VerificationOtpRepository extends JpaRepository<VerificationOtp, Long> {

    Optional<VerificationOtp> findTopByEmailOrderByIdDesc(String email);

    @Modifying
    @Transactional
    void deleteByEmail(String email);
}
