package backend.back.service;

import backend.back.entity.User;
import backend.back.exception.ValidationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AuthMailService {

    private final JavaMailSender mailSender;

    @Value("${app.auth.public-base-url:http://127.0.0.1:3000}")
    private String publicBaseUrl;

    @Value("${app.auth.mail-from:no-reply@stroyraschet.local}")
    private String mailFrom;

    public void sendVerificationEmail(User user) {
        String verificationLink = normalizeBaseUrl() + "/api/auth/verify-email?token=" + user.getEmailVerificationToken();
        String greeting = user.getFirstName() == null ? user.getLogin() : user.getFirstName();
        sendHtmlMessage(
                user.getEmail(),
                "Подтвердите email в СтройРасчёт",
                """
                        <p>Здравствуйте, %s.</p>
                        <p>Ваш аккаунт создан. Подтвердите email, чтобы завершить регистрацию.</p>
                        <p><a href="%s">Подтвердить email</a></p>
                        <p>Если кнопку не видно, скопируйте ссылку вручную:<br>%s</p>
                        """.formatted(greeting, verificationLink, verificationLink)
        );
    }

    public void sendPasswordResetEmail(User user) {
        String resetLink = normalizeBaseUrl() + "/api/auth/password/reset?token=" + user.getPasswordResetToken();
        String greeting = user.getFirstName() == null ? user.getLogin() : user.getFirstName();
        sendHtmlMessage(
                user.getEmail(),
                "Смена пароля в СтройРасчёт",
                """
                        <p>Здравствуйте, %s.</p>
                        <p>Мы получили запрос на смену пароля. Чтобы задать новый пароль, откройте ссылку ниже.</p>
                        <p><a href="%s">Задать новый пароль</a></p>
                        <p>Если кнопку не видно, скопируйте ссылку вручную:<br>%s</p>
                        """.formatted(greeting, resetLink, resetLink)
        );
    }

    private void sendHtmlMessage(String email, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(mailFrom);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(buildHtmlDocument(subject, body), true);
            mailSender.send(message);
        } catch (MailException | MessagingException ex) {
            throw new ValidationException("Не удалось отправить письмо. Проверьте настройки MailHog/SMTP.");
        }
    }

    private String buildHtmlDocument(String title, String body) {
        return """
                <!doctype html>
                <html lang="ru">
                  <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                  </head>
                  <body style="margin:0;padding:32px;background:#0b1325;color:#f4f6fb;font-family:Segoe UI,Arial,sans-serif;">
                    <div style="max-width:640px;margin:0 auto;padding:28px;border-radius:24px;background:#151d31;border:1px solid rgba(255,255,255,.08);">
                      <p style="margin:0 0 12px;color:#ffb257;font-weight:700;">СтройРасчёт</p>
                      <h1 style="margin:0 0 18px;font-size:28px;">%s</h1>
                      <div style="color:#dbe4f4;line-height:1.7;">%s</div>
                    </div>
                  </body>
                </html>
                """.formatted(title, title, body);
    }

    private String normalizeBaseUrl() {
        return publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
    }
}
