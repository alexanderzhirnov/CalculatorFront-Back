package backend.back.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConcreteToolRequest {

    @NotNull
    @DecimalMin("0.1")
    private Double length;

    @NotNull
    @DecimalMin("0.1")
    private Double width;

    @NotNull
    @DecimalMin("1.0")
    private Double depthCm;
}
