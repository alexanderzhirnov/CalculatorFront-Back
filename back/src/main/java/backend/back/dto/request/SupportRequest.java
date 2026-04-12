package backend.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupportRequest {

    @NotBlank(message = "Имя обязательно")
    @Size(max = 80, message = "Имя не должно превышать 80 символов")
    private String name;

    @NotBlank(message = "Контакт обязателен")
    @Size(max = 120, message = "Контакт не должен превышать 120 символов")
    private String contact;

    @NotBlank(message = "Сообщение обязательно")
    @Size(max = 1500, message = "Сообщение не должно превышать 1500 символов")
    private String message;

    @Size(max = 80, message = "Источник не должен превышать 80 символов")
    private String source;

    @Size(max = 80, message = "Страница не должна превышать 80 символов")
    private String page;
}
