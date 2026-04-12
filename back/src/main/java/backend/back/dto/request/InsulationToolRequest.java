package backend.back.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InsulationToolRequest {

    @NotNull
    @DecimalMin("0.1")
    private Double area;

    @NotNull
    @DecimalMin("1.0")
    private Double thicknessMm;

    @NotNull
    @DecimalMin("0.1")
    private Double packVolume;
}
