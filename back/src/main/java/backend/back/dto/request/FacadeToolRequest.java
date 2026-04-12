package backend.back.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FacadeToolRequest {

    @NotNull
    @DecimalMin("0.1")
    private Double width;

    @NotNull
    @DecimalMin("0.1")
    private Double height;

    @NotNull
    @DecimalMin("1.0")
    private Double pricePerSquareMeter;
}
