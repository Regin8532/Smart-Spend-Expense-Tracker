package com.example.ExpenseTracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;

    @InjectMocks EmailService emailService;

    @Test
    void send_buildsMessage_andCallsMailSender() {
        emailService.send("a@test.com", "Sub", "Body");

        ArgumentCaptor<SimpleMailMessage> cap = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(cap.capture());

        SimpleMailMessage msg = cap.getValue();
        assertThat(msg.getTo()).containsExactly("a@test.com");
        assertThat(msg.getSubject()).isEqualTo("Sub");
        assertThat(msg.getText()).isEqualTo("Body");
    }
}
