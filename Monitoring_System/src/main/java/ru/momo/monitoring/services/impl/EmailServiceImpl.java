package ru.momo.monitoring.services.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.services.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String email, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String confirmationLink = "http://localhost:8081/api/v1/auth/confirm/" + token;
            String emailContent = """
                    <h3>Подтвердите ваш email</h3>
                    <p>Для подтверждения регистрации, перейдите по ссылке:</p>
                    <a href="%s">Подтвердить email</a>
                    """.formatted(confirmationLink);

            helper.setTo(email);
            helper.setSubject("Подтверждение регистрации");
            helper.setText(emailContent, true);
            helper.setFrom("morgachevctepan@yandex.ru");

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Ошибка при отправке email", e);
        }
    }
}

