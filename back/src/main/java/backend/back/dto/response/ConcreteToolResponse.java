package backend.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ConcreteToolResponse {
    private double volume;
    private int mixers;
    private BigDecimal budget;
}
