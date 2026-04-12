package backend.back.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SheetToolRequest {

    @NotNull
    @DecimalMin("0.1")
    private Double roomLength;

    @NotNull
    @DecimalMin("0.1")
    private Double roomWidth;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("30.0")
    private Double reservePercent;
}
