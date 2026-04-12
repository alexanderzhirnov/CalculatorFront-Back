package backend.back.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientRequest {

    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 100, message = "Фамилия не должна превышать 100 символов")
    private String lastName;

    @NotBlank(message = "Имя обязательно")
    @Size(max = 100, message = "Имя не должно превышать 100 символов")
    private String firstName;

    @Size(max = 100, message = "Отчество не должно превышать 100 символов")
    private String patronymic;

    @Pattern(regexp = "^(\\s*|\\+?[0-9()\\-\\s]+)$", message = "Телефон может содержать только цифры, пробелы, скобки и дефис")
    private String phone;

    @Email(message = "Укажите корректный email клиента")
    @Size(max = 150, message = "Email не должен превышать 150 символов")
    private String email;

    @Size(max = 255, message = "Адрес не должен превышать 255 символов")
    private String address;
}
