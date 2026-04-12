package backend.back.controller;

import backend.back.dto.request.EmailRequest;
import backend.back.dto.request.LoginRequest;
import backend.back.dto.request.RegisterRequest;
import backend.back.dto.response.AuthResponse;
import backend.back.dto.response.MessageResponse;
import backend.back.exception.ValidationException;
import backend.back.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    @Operation(summary = "Метод не поддерживается (используйте POST)")
    public ResponseEntity<Void> loginGet() {
        return ResponseEntity.status(405).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.accepted().body(authService.register(request));
    }

    @PostMapping("/verification/resend")
    @Operation(summary = "Повторная отправка письма подтверждения")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(authService.resendVerification(request));
    }

    @PostMapping("/password/forgot")
    @Operation(summary = "Отправить ссылку для смены пароля")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(authService.requestPasswordReset(request));
    }

    @GetMapping(value = "/verify-email", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Подтверждение email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            return ResponseEntity.ok(renderResultPage("Email подтверждён", authService.verifyEmail(token), true));
        } catch (ValidationException ex) {
            return ResponseEntity.ok(renderResultPage("Подтверждение не выполнено", ex.getMessage(), false));
        }
    }

    @GetMapping(value = "/password/reset", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Форма смены пароля")
    public ResponseEntity<String> passwordResetForm(@RequestParam String token) {
        try {
            authService.ensurePasswordResetTokenIsActive(token);
            return ResponseEntity.ok(renderPasswordResetForm(token));
        } catch (ValidationException ex) {
            return ResponseEntity.ok(renderResultPage("Ссылка недействительна", ex.getMessage(), false));
        }
    }

    @PostMapping(value = "/password/reset", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Сохранить новый пароль")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String password) {
        try {
            return ResponseEntity.ok(renderResultPage("Пароль обновлён", authService.resetPassword(token, password), true));
        } catch (ValidationException ex) {
            return ResponseEntity.ok(renderResultPage("Пароль не изменён", ex.getMessage(), false));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление access-токена по refresh-токену")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    private String renderPasswordResetForm(String token) {
        String safeToken = HtmlUtils.htmlEscape(token);
        return """
                <!doctype html>
                <html lang="ru">
                  <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Смена пароля</title>
                    <style>
                      body{margin:0;padding:32px;background:#091223;color:#edf2fb;font-family:Segoe UI,Arial,sans-serif}
                      .wrap{max-width:520px;margin:0 auto;padding:28px;border-radius:24px;background:#161f33;border:1px solid rgba(255,255,255,.08)}
                      h1{margin:0 0 12px;font-size:28px}
                      p{color:#c9d4e7;line-height:1.6}
                      label{display:grid;gap:8px;margin-top:18px}
                      input{width:100%%;box-sizing:border-box;padding:14px 16px;border-radius:16px;border:1px solid rgba(255,255,255,.12);background:#232c40;color:#fff}
                      button{margin-top:18px;padding:14px 18px;border:none;border-radius:16px;background:#ff9c45;color:#111;font-weight:700;cursor:pointer}
                    </style>
                  </head>
                  <body>
                    <main class="wrap">
                      <p style="margin:0 0 8px;color:#ffb257;font-weight:700;">СтройРасчёт</p>
                      <h1>Задайте новый пароль</h1>
                      <p>Пароль должен быть длиной от 8 до 72 символов и содержать хотя бы одну букву и одну цифру.</p>
                      <form method="post" action="/api/auth/password/reset">
                        <input type="hidden" name="token" value="%s">
                        <label>
                          <span>Новый пароль</span>
                          <input type="password" name="password" minlength="8" maxlength="72" required autocomplete="new-password">
                        </label>
                        <button type="submit">Сохранить пароль</button>
                      </form>
                    </main>
                  </body>
                </html>
                """.formatted(safeToken);
    }

    private String renderResultPage(String title, String message, boolean success) {
        String accent = success ? "#68d192" : "#ff8b6a";
        String safeTitle = HtmlUtils.htmlEscape(title);
        String safeMessage = HtmlUtils.htmlEscape(message);
        return """
                <!doctype html>
                <html lang="ru">
                  <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                      body{margin:0;padding:32px;background:#091223;color:#edf2fb;font-family:Segoe UI,Arial,sans-serif}
                      .wrap{max-width:540px;margin:0 auto;padding:28px;border-radius:24px;background:#161f33;border:1px solid rgba(255,255,255,.08)}
                      .pill{display:inline-block;padding:6px 12px;border-radius:999px;background:rgba(255,255,255,.06);color:%s;font-weight:700}
                      h1{margin:14px 0 12px;font-size:28px}
                      p{color:#c9d4e7;line-height:1.7}
                      a{display:inline-flex;margin-top:12px;color:#111;background:#ffb257;padding:12px 16px;border-radius:14px;text-decoration:none;font-weight:700}
                    </style>
                  </head>
                  <body>
                    <main class="wrap">
                      <span class="pill">СтройРасчёт</span>
                      <h1>%s</h1>
                      <p>%s</p>
                      <a href="/">Вернуться в приложение</a>
                    </main>
                  </body>
                </html>
                """.formatted(safeTitle, accent, safeTitle, safeMessage);
    }
}
