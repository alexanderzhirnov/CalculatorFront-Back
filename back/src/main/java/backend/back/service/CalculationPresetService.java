package backend.back.service;

import backend.back.dto.request.FoundationParamsRequest;
import backend.back.dto.request.FoundationPresetRequest;
import backend.back.dto.request.FrameParamsRequest;
import backend.back.dto.request.FramePresetRequest;
import backend.back.dto.request.OpeningDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class CalculationPresetService {

    private static final String DEFAULT_OSB = "OSB 9 мм";
    private static final String DEFAULT_VAPOR = "Ондутис";
    private static final String DEFAULT_WIND_BARRIER = "Паро-проницаемая ветро-влагозащита A Optima";
    private static final String DEFAULT_CEILING_WIND_BARRIER = "Гидро-ветрозащита Тип А";
    private static final String DEFAULT_PILE_TYPE = "Бетонные сваи 200*200*3000";
    private static final String DEFAULT_CONCRETE_GRADE = "М300 (В22.5)";

    private static final Map<FrameParamsRequest.ExtWallThickness, String> WALL_INSULATION_BY_THICKNESS = Map.of(
            FrameParamsRequest.ExtWallThickness.MM_100, "Эковер 100 мм",
            FrameParamsRequest.ExtWallThickness.MM_150, "Эковер 150 мм",
            FrameParamsRequest.ExtWallThickness.MM_200, "Эковер 200 мм",
            FrameParamsRequest.ExtWallThickness.MM_250, "Эковер 250 мм"
    );

    private static final Map<FrameParamsRequest.CeilingThickness, String> CEILING_INSULATION_BY_THICKNESS = Map.of(
            FrameParamsRequest.CeilingThickness.MM_200, "Эковер 200 мм",
            FrameParamsRequest.CeilingThickness.MM_250, "Эковер 250 мм"
    );

    public FrameParamsRequest buildFrameRequest(FramePresetRequest preset) {
        FrameParamsRequest request = new FrameParamsRequest();
        request.setFloors(preset.getFloors());
        request.setFloorParamsList(IntStream.range(0, preset.getFloors())
                .mapToObj(index -> createFloorParams(preset))
                .toList());
        return request;
    }

    public FoundationParamsRequest buildFoundationRequest(FoundationPresetRequest preset) {
        FoundationParamsRequest request = new FoundationParamsRequest();
        request.setExternalPerimeter(preset.getExternalPerimeter());
        request.setInnerWallLength(preset.getInnerWallLength());
        request.setPileType(DEFAULT_PILE_TYPE);
        request.setConcreteGrade(DEFAULT_CONCRETE_GRADE);
        return request;
    }

    private FrameParamsRequest.FloorParams createFloorParams(FramePresetRequest preset) {
        FrameParamsRequest.FloorParams floorParams = new FrameParamsRequest.FloorParams();
        floorParams.setFloorHeight(preset.getFloorHeight());
        floorParams.setPerimeter(preset.getPerimeter());
        floorParams.setFoundationArea(preset.getFoundationArea());
        floorParams.setExtWallThickness(preset.getExtWallThickness());
        floorParams.setInnerWallLength(preset.getInnerWallLength());
        floorParams.setIntWallThickness(preset.getIntWallThickness());
        floorParams.setExtOsb(DEFAULT_OSB);
        floorParams.setExtVapor(DEFAULT_VAPOR);
        floorParams.setExtWindBarrier(DEFAULT_WIND_BARRIER);
        floorParams.setExtInsulation(WALL_INSULATION_BY_THICKNESS.getOrDefault(
                preset.getExtWallThickness(),
                "Эковер 150 мм"));
        floorParams.setIntOsb(DEFAULT_OSB);
        floorParams.setCeilingThickness(preset.getCeilingThickness());
        floorParams.setCeilingOsb(DEFAULT_OSB);
        floorParams.setCeilingVapor(DEFAULT_VAPOR);
        floorParams.setCeilingWindBarrier(DEFAULT_CEILING_WIND_BARRIER);
        floorParams.setCeilingInsulation(CEILING_INSULATION_BY_THICKNESS.getOrDefault(
                preset.getCeilingThickness(),
                "Эковер 200 мм"));
        floorParams.setWindows(List.of(opening(1.4, 1.4, 8)));
        floorParams.setExternalDoors(List.of(opening(1.0, 2.1, 2)));
        floorParams.setInternalDoors(List.of(opening(0.9, 2.1, 6)));
        return floorParams;
    }

    private OpeningDto opening(double width, double height, int quantity) {
        OpeningDto opening = new OpeningDto();
        opening.setWidth(width);
        opening.setHeight(height);
        opening.setQuantity(quantity);
        return opening;
    }
}
