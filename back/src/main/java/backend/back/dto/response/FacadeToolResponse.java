package backend.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FacadeToolResponse {
    private double area;
    private double reserveArea;
    private BigDecimal total;
}
