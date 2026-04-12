package backend.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SheetToolResponse {
    private double area;
    private double actualArea;
    private int sheets;
    private BigDecimal budget;
}
