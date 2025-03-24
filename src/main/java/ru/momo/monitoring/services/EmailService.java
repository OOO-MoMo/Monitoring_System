package ru.momo.monitoring.services;

public interface EmailService {
    void sendEmail(String email, String token);
}
