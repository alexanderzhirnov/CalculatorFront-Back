package backend.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class InsulationToolResponse {
    private double volume;
    private int packs;
    private BigDecimal budget;
}
