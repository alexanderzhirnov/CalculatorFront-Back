package backend.back.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoder that supports both BCrypt hashes and legacy/plain-text passwords.
 *
 * - If stored password looks like BCrypt ($2a$/$2b$/$2y$), uses BCrypt verification.
 * - Otherwise falls back to plain string comparison (useful for existing dev DBs with unencoded passwords).
 *
 * IMPORTANT: keep plain-text fallback only if you truly need backward compatibility.
 * Best practice is to store only BCrypt hashes.
 */
public class FlexiblePasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        if (isBcryptHash(encodedPassword)) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }
        // Legacy fallback (plain text / other non-bcrypt formats)
        return rawPassword != null && rawPassword.toString().equals(encodedPassword);
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
