package ru.momo.monitoring.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import ru.momo.monitoring.services.impl.EmailServiceImpl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendEmail_ShouldSendEmailSuccessfully() throws Exception {
        String email = "test@example.com";
        String token = "123abc";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(email, token);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }
}
