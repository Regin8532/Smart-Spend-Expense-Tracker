package com.example.ExpenseTracker.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
public class EmailService {

    private final String apiKey;
    private final String fromEmail;
    private final String fromName;

    public EmailService(
            @Value("${sendgrid.api-key}") String apiKey,
            @Value("${mail.from}") String fromEmail,
            @Value("${mail.from-name:SmartSpend}") String fromName
    ) {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("SENDGRID_API_KEY missing");
        if (fromEmail == null || fromEmail.isBlank()) throw new IllegalStateException("MAIL_FROM missing");

        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    public void send(String to, String subject, String text) {
        Email from = new Email(fromEmail, fromName);
        Email toEmail = new Email(to);

        Content content = new Content("text/plain", text);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(apiKey);

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() != 202) {
                throw new RuntimeException("SendGrid failed: status=" + response.getStatusCode()
                        + " body=" + response.getBody());
            }
        } catch (IOException e) {
            throw new RuntimeException("SendGrid error: " + e.getMessage(), e);
        }
    }
}
