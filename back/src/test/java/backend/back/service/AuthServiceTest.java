package backend.back.service;

import backend.back.dto.request.EmailRequest;
import backend.back.dto.request.LoginRequest;
import backend.back.dto.request.RegisterRequest;
import backend.back.dto.response.AuthResponse;
import backend.back.dto.response.MessageResponse;
import backend.back.entity.User;
import backend.back.entity.enums.Role;
import backend.back.repository.UserRepository;
import backend.back.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthMailService authMailService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "emailVerificationTtlHours", 24L);
        ReflectionTestUtils.setField(authService, "passwordResetTtlMinutes", 30L);
    }

    @Test
    void registerCreatesUnverifiedUserAndSendsVerificationEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setLogin(" Manager.Petrov ");
        request.setEmail(" USER@example.ru ");
        request.setPassword("Secure123");
        request.setFirstName("Иван");
        request.setLastName("Петров");

        when(userRepository.existsByLoginIgnoreCase("manager.petrov")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("user@example.ru")).thenReturn(false);
        when(passwordEncoder.encode("Secure123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(authMailService).sendVerificationEmail(userCaptor.getValue());

        User savedUser = userCaptor.getValue();
        assertThat(response.getMessage()).contains("MailHog");
        assertThat(savedUser.getLogin()).isEqualTo("manager.petrov");
        assertThat(savedUser.getEmail()).isEqualTo("user@example.ru");
        assertThat(savedUser.isEmailVerified()).isFalse();
        assertThat(savedUser.getEmailVerificationToken()).isNotBlank();
        assertThat(savedUser.getEmailVerificationExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(savedUser.getRole()).isEqualTo(Role.MANAGER);
    }

    @Test
    void loginAcceptsEmailAsIdentifier() {
        LoginRequest request = new LoginRequest();
        request.setLogin("user@example.ru");
        request.setPassword("Secure123");

        User user = new User();
        user.setLogin("manager.petrov");
        user.setEmail("user@example.ru");
        user.setFirstName("Иван");
        user.setLastName("Петров");
        user.setPassword("encoded-password");
        user.setRole(Role.MANAGER);
        user.setEmailVerified(true);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("manager.petrov")
                .password("encoded-password")
                .authorities("ROLE_MANAGER")
                .build();

        when(userRepository.findByEmailIgnoreCase("user@example.ru")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("manager.petrov")).thenReturn(userDetails);
        when(userRepository.findByLoginIgnoreCase("manager.petrov")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(any());
        assertThat(response.getLogin()).isEqualTo("manager.petrov");
        assertThat(response.getEmail()).isEqualTo("user@example.ru");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void requestPasswordResetResendsVerificationForUnconfirmedAccount() {
        EmailRequest request = new EmailRequest();
        request.setEmail("user@example.ru");

        User user = new User();
        user.setLogin("manager.petrov");
        user.setEmail("user@example.ru");
        user.setFirstName("Иван");
        user.setEmailVerified(false);

        when(userRepository.findByEmailIgnoreCase("user@example.ru")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = authService.requestPasswordReset(request);

        verify(authMailService).sendVerificationEmail(user);
        verify(authMailService, never()).sendPasswordResetEmail(any(User.class));
        assertThat(response.getMessage()).contains("подтвержд");
        assertThat(user.getEmailVerificationToken()).isNotBlank();
        assertThat(user.getPasswordResetToken()).isNull();
    }

    @Test
    void resetPasswordUpdatesPasswordAndClearsResetToken() {
        User user = new User();
        user.setLogin("manager.petrov");
        user.setEmail("user@example.ru");
        user.setEmailVerified(true);
        user.setPasswordResetToken("token-123");
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(15));

        when(userRepository.findByPasswordResetToken("token-123")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Secure123")).thenReturn("encoded-password");

        String message = authService.resetPassword("token-123", "Secure123");

        verify(userRepository).save(eq(user));
        assertThat(message).contains("Пароль обновлён");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getPasswordResetToken()).isNull();
        assertThat(user.getPasswordResetExpiresAt()).isNull();
    }

    @Test
    void verifyEmailMarksUserAsConfirmed() {
        User user = new User();
        user.setLogin("manager.petrov");
        user.setEmail("user@example.ru");
        user.setEmailVerified(false);
        user.setEmailVerificationToken("verify-token");
        user.setEmailVerificationExpiresAt(LocalDateTime.now().plusHours(2));

        when(userRepository.findByEmailVerificationToken("verify-token")).thenReturn(Optional.of(user));

        String message = authService.verifyEmail("verify-token");

        verify(userRepository).save(eq(user));
        assertThat(message).contains("Email подтверждён");
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getEmailVerificationToken()).isNull();
        assertThat(user.getEmailVerificationExpiresAt()).isNull();
    }
}
