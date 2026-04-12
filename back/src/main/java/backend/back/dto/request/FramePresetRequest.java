package backend.back.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FramePresetRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer floors;

    @NotNull
    @DecimalMin("2.0")
    @DecimalMax("3.5")
    private Double floorHeight;

    @NotNull
    @Positive
    private Double perimeter;

    @NotNull
    @Positive
    private Double foundationArea;

    @NotNull
    @Positive
    private Double innerWallLength;

    @NotNull
    private FrameParamsRequest.ExtWallThickness extWallThickness;

    @NotNull
    private FrameParamsRequest.IntWallThickness intWallThickness;

    @NotNull
    private FrameParamsRequest.CeilingThickness ceilingThickness;
}
