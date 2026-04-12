package backend.back.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FoundationPresetRequest {

    @NotNull
    @Positive
    private Double externalPerimeter;

    @NotNull
    @Positive
    private Double innerWallLength;
}
