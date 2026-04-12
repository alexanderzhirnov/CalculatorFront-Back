package backend.back.service;

import backend.back.dto.request.EmailRequest;
import backend.back.dto.request.LoginRequest;
import backend.back.dto.request.RegisterRequest;
import backend.back.dto.response.AuthResponse;
import backend.back.dto.response.MessageResponse;
import backend.back.entity.User;
import backend.back.entity.enums.Role;
import backend.back.exception.ValidationException;
import backend.back.repository.UserRepository;
import backend.back.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,100}$");
    private static final Pattern PASSWORD_LETTER_PATTERN = Pattern.compile(".*[A-Za-zА-Яа-я].*");
    private static final Pattern PASSWORD_DIGIT_PATTERN = Pattern.compile(".*\\d.*");

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMailService authMailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.auth.email-verification-ttl-hours:24}")
    private long emailVerificationTtlHours;

    @Value("${app.auth.password-reset-ttl-minutes:30}")
    private long passwordResetTtlMinutes;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = normalizeRequired(request.getLogin(), "Логин или email обязателен");
        String login = resolveLogin(identifier);

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, request.getPassword()));
        return buildAuthResponse(login);
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        String login = normalizeLogin(request.getLogin());
        String email = normalizeEmail(request.getEmail());
        String firstName = normalizeRequired(request.getFirstName(), "Имя обязательно");
        String lastName = normalizeRequired(request.getLastName(), "Фамилия обязательна");

        validatePasswordStrength(request.getPassword());

        if (userRepository.existsByLoginIgnoreCase(login)) {
            throw new ValidationException("Логин уже занят: " + login);
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ValidationException("Email уже используется: " + email);
        }

        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.MANAGER);
        user.setCreatedAt(LocalDateTime.now());
        user.setEmailVerified(false);
        issueEmailVerification(user);

        userRepository.save(user);
        authMailService.sendVerificationEmail(user);

        return new MessageResponse("Аккаунт создан. Подтвердите email через письмо, отправленное в MailHog.");
    }

    @Transactional
    public MessageResponse resendVerification(EmailRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null) {
            return new MessageResponse("Если такой email существует, письмо с подтверждением уже отправлено.");
        }
        if (user.isEmailVerified()) {
            return new MessageResponse("Email уже подтверждён. Можно входить в систему.");
        }

        issueEmailVerification(user);
        userRepository.save(user);
        authMailService.sendVerificationEmail(user);
        return new MessageResponse("Письмо с подтверждением отправлено повторно.");
    }

    @Transactional
    public MessageResponse requestPasswordReset(EmailRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null) {
            return new MessageResponse("Если такой email существует, инструкция по смене пароля уже отправлена.");
        }
        if (!user.isEmailVerified()) {
            issueEmailVerification(user);
            userRepository.save(user);
            authMailService.sendVerificationEmail(user);
            return new MessageResponse("Email ещё не подтверждён. Мы отправили новое письмо для подтверждения.");
        }

        issuePasswordReset(user);
        userRepository.save(user);
        authMailService.sendPasswordResetEmail(user);
        return new MessageResponse("Инструкция по смене пароля отправлена на почту.");
    }

    @Transactional
    public String verifyEmail(String token) {
        User user = getUserByVerificationToken(token);

        if (user.isEmailVerified()) {
            return "Email уже подтверждён. Можно возвращаться в приложение и входить в систему.";
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiresAt(null);
        userRepository.save(user);
        return "Email подтверждён. Теперь можно войти под логином или email.";
    }

    @Transactional(readOnly = true)
    public void ensurePasswordResetTokenIsActive(String token) {
        getUserByPasswordResetToken(token);
    }

    @Transactional
    public String resetPassword(String token, String newPassword) {
        validatePasswordStrength(newPassword);

        User user = getUserByPasswordResetToken(token);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);
        return "Пароль обновлён. Теперь можно вернуться в приложение и войти заново.";
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new ValidationException("Refresh token невалиден");
        }

        String login = jwtTokenProvider.extractUsername(refreshToken);
        return buildAuthResponse(login);
    }

    private AuthResponse buildAuthResponse(String login) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(login);
        User user = userRepository.findByLoginIgnoreCase(login).orElseThrow();

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getLogin(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    private String resolveLogin(String identifier) {
        return userRepository.findByEmailIgnoreCase(identifier)
                .map(User::getLogin)
                .or(() -> userRepository.findByLoginIgnoreCase(identifier).map(User::getLogin))
                .orElse(identifier);
    }

    private User getUserByVerificationToken(String token) {
        User user = userRepository.findByEmailVerificationToken(requireToken(token))
                .orElseThrow(() -> new ValidationException("Ссылка подтверждения недействительна или уже устарела"));

        if (user.getEmailVerificationExpiresAt() == null || user.getEmailVerificationExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Срок действия ссылки подтверждения истёк. Запросите письмо повторно.");
        }
        return user;
    }

    private User getUserByPasswordResetToken(String token) {
        User user = userRepository.findByPasswordResetToken(requireToken(token))
                .orElseThrow(() -> new ValidationException("Ссылка для смены пароля недействительна или уже устарела"));

        if (user.getPasswordResetExpiresAt() == null || user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Срок действия ссылки для смены пароля истёк. Запросите новую.");
        }
        return user;
    }

    private void issueEmailVerification(User user) {
        user.setEmailVerified(false);
        user.setEmailVerificationToken(generateToken());
        user.setEmailVerificationExpiresAt(LocalDateTime.now().plusHours(emailVerificationTtlHours));
    }

    private void issuePasswordReset(User user) {
        user.setPasswordResetToken(generateToken());
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(passwordResetTtlMinutes));
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("Пароль обязателен");
        }
        if (password.length() < 8 || password.length() > 72) {
            throw new ValidationException("Пароль должен быть от 8 до 72 символов");
        }
        if (!PASSWORD_LETTER_PATTERN.matcher(password).matches() || !PASSWORD_DIGIT_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Пароль должен содержать хотя бы одну букву и одну цифру");
        }
    }

    private String normalizeLogin(String value) {
        String normalized = normalizeRequired(value, "Логин обязателен").toLowerCase(Locale.ROOT);
        if (!LOGIN_PATTERN.matcher(normalized).matches()) {
            throw new ValidationException("Логин может содержать только латиницу, цифры, точку, дефис и подчёркивание");
        }
        return normalized;
    }

    private String normalizeEmail(String value) {
        return normalizeRequired(value, "Email обязателен").toLowerCase(Locale.ROOT);
    }

    private String requireToken(String value) {
        return normalizeRequired(value, "Токен обязателен");
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new ValidationException(message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replaceAll("\\s{2,}", " ");
        return normalized.isEmpty() ? null : normalized;
    }
}
