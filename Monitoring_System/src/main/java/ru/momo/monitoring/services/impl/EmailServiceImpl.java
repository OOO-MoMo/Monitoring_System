package ru.momo.monitoring.services.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.services.EmailService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${application.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String mailFrom;

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

    @Override
    public void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(mailFrom);

            mailSender.send(message);
            log.info("Email sent successfully to {} with subject '{}'", toEmail, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {} with subject '{}': {}", toEmail, subject, e.getMessage(), e);
            throw new RuntimeException("Ошибка при отправке email: " + e.getMessage(), e);
        }
    }

    public String createConfirmationEmailHtml(String recipientName, String confirmationLink) {
        String namePart = (recipientName != null && !recipientName.isBlank()) ? recipientName : "Пользователь";

        return String.format("""
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Подтверждение Email</title>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 20px auto; padding: 20px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
                        .header { text-align: center; padding-bottom: 20px; border-bottom: 1px solid #eeeeee; }
                        .header h1 { color: #2c3e50; margin: 0; }
                        .content { padding: 20px 0; }
                        .content p { margin-bottom: 15px; }
                        .button-container { text-align: center; margin-top: 25px; margin-bottom: 25px; }
                        .button { background-color: #3498db; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; }
                        .button:hover { background-color: #2980b9; }
                        .footer { text-align: center; padding-top: 20px; border-top: 1px solid #eeeeee; font-size: 0.9em; color: #7f8c8d; }
                        .footer p { margin: 5px 0; }
                        .link { color: #3498db; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Подтверждение Вашего Email адреса</h1>
                        </div>
                        <div class="content">
                            <p>Здравствуйте, %s!</p>
                            <p>Благодарим Вас за регистрацию в Системе Мониторинга. Пожалуйста, подтвердите Ваш адрес электронной почты, чтобы активировать Вашу учетную запись и получить доступ ко всем функциям системы.</p>
                            <p>Для подтверждения, пожалуйста, нажмите на кнопку ниже или перейдите по следующей ссылке:</p>
                            <div class="button-container">
                                <a href="%s" class="button">Подтвердить Email</a>
                            </div>
                            <p>Если кнопка не работает, скопируйте и вставьте эту ссылку в адресную строку Вашего браузера:</p>
                            <p><a href="%s" class="link">%s</a></p>
                            <p>Если Вы не регистрировались в нашей системе, пожалуйста, проигнорируйте это письмо.</p>
                            <p>Срок действия этой ссылки для подтверждения ограничен (обычно 1 час).</p>
                        </div>
                        <div class="footer">
                            <p>С уважением,<br>Команда Системы Мониторинга</p>
                            <p>© %s MoMo. Все права защищены.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, namePart, confirmationLink, confirmationLink, confirmationLink, LocalDateTime.now().getYear());
    }
}

