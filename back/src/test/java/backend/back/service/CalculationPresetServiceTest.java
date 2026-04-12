package backend.back.service;

import backend.back.dto.request.FoundationParamsRequest;
import backend.back.dto.request.FoundationPresetRequest;
import backend.back.dto.request.FrameParamsRequest;
import backend.back.dto.request.FramePresetRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CalculationPresetServiceTest {

    private final CalculationPresetService service = new CalculationPresetService();

    @Test
    void buildFrameRequestExpandsPresetIntoFullRequest() {
        FramePresetRequest preset = new FramePresetRequest();
        preset.setFloors(2);
        preset.setFloorHeight(2.8);
        preset.setPerimeter(48.0);
        preset.setFoundationArea(120.0);
        preset.setInnerWallLength(36.0);
        preset.setExtWallThickness(FrameParamsRequest.ExtWallThickness.MM_200);
        preset.setIntWallThickness(FrameParamsRequest.IntWallThickness.MM_100);
        preset.setCeilingThickness(FrameParamsRequest.CeilingThickness.MM_250);

        FrameParamsRequest request = service.buildFrameRequest(preset);

        assertThat(request.getFloors()).isEqualTo(2);
        assertThat(request.getFloorParamsList()).hasSize(2);
        assertThat(request.getFloorParamsList()).allSatisfy(floor -> {
            assertThat(floor.getFloorHeight()).isEqualTo(2.8);
            assertThat(floor.getPerimeter()).isEqualTo(48.0);
            assertThat(floor.getFoundationArea()).isEqualTo(120.0);
            assertThat(floor.getInnerWallLength()).isEqualTo(36.0);
            assertThat(floor.getExtWallThickness()).isEqualTo(200);
            assertThat(floor.getIntWallThickness()).isEqualTo(100);
            assertThat(floor.getCeilingThickness()).isEqualTo(250);
            assertThat(floor.getExtOsb()).isEqualTo("OSB 9 мм");
            assertThat(floor.getExtInsulation()).isEqualTo("Эковер 200 мм");
            assertThat(floor.getCeilingInsulation()).isEqualTo("Эковер 250 мм");
            assertThat(floor.getWindows()).singleElement().satisfies(opening -> {
                assertThat(opening.getWidth()).isEqualTo(1.4);
                assertThat(opening.getHeight()).isEqualTo(1.4);
                assertThat(opening.getQuantity()).isEqualTo(8);
            });
            assertThat(floor.getExternalDoors()).singleElement().satisfies(opening -> {
                assertThat(opening.getWidth()).isEqualTo(1.0);
                assertThat(opening.getHeight()).isEqualTo(2.1);
                assertThat(opening.getQuantity()).isEqualTo(2);
            });
            assertThat(floor.getInternalDoors()).singleElement().satisfies(opening -> {
                assertThat(opening.getWidth()).isEqualTo(0.9);
                assertThat(opening.getHeight()).isEqualTo(2.1);
                assertThat(opening.getQuantity()).isEqualTo(6);
            });
        });
    }

    @Test
    void buildFoundationRequestAddsServerDefaults() {
        FoundationPresetRequest preset = new FoundationPresetRequest();
        preset.setExternalPerimeter(48.0);
        preset.setInnerWallLength(36.0);

        FoundationParamsRequest request = service.buildFoundationRequest(preset);

        assertThat(request.getExternalPerimeter()).isEqualTo(48.0);
        assertThat(request.getInnerWallLength()).isEqualTo(36.0);
        assertThat(request.getPileType()).isEqualTo("Бетонные сваи 200*200*3000");
        assertThat(request.getConcreteGrade()).isEqualTo("М300 (В22.5)");
    }
}
