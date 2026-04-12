package backend.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CalculationRequest {
    @NotBlank(message = "Адрес объекта обязателен")
    @Size(min = 5, max = 255, message = "Адрес объекта должен быть от 5 до 255 символов")
    private String constructionAddress;
}
