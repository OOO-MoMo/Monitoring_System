package ru.momo.monitoring.services;

public interface EmailService {
    void sendEmail(String email, String token);

    void sendEmail(String toEmail, String subject, String htmlContent);

    String createConfirmationEmailHtml(String recipientName, String confirmationLink);
}
