package backend.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Логин или email обязателен")
    @Size(max = 150, message = "Логин или email не должен превышать 150 символов")
    private String login;

    @NotBlank(message = "Пароль обязателен")
    @Size(max = 72, message = "Пароль не должен превышать 72 символа")
    private String password;
}
