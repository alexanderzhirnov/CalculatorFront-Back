package backend.back.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import backend.back.entity.enums.Role;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @Column(nullable = false)
    private boolean emailVerified;

    private String emailVerificationToken;
    private LocalDateTime emailVerificationExpiresAt;
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiresAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;
}
