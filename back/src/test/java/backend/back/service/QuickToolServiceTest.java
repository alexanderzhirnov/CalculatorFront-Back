package backend.back.service;

import backend.back.dto.request.ConcreteToolRequest;
import backend.back.dto.request.FacadeToolRequest;
import backend.back.dto.request.InsulationToolRequest;
import backend.back.dto.request.SheetToolRequest;
import backend.back.dto.response.ConcreteToolResponse;
import backend.back.dto.response.FacadeToolResponse;
import backend.back.dto.response.InsulationToolResponse;
import backend.back.dto.response.SheetToolResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuickToolServiceTest {

    private final QuickToolService quickToolService = new QuickToolService();

    @Test
    void calculatesFacadeMetrics() {
        FacadeToolRequest request = new FacadeToolRequest();
        request.setWidth(9.6);
        request.setHeight(3.1);
        request.setPricePerSquareMeter(980.0);

        FacadeToolResponse response = quickToolService.calculateFacade(request);

        assertEquals(29.76, response.getArea(), 0.001);
        assertEquals(32.14, response.getReserveArea(), 0.001);
        assertEquals(new BigDecimal("31497.98"), response.getTotal());
    }

    @Test
    void calculatesConcreteMetrics() {
        ConcreteToolRequest request = new ConcreteToolRequest();
        request.setLength(11.0);
        request.setWidth(8.0);
        request.setDepthCm(30.0);

        ConcreteToolResponse response = quickToolService.calculateConcrete(request);

        assertEquals(26.4, response.getVolume(), 0.001);
        assertEquals(4, response.getMixers());
        assertEquals(new BigDecimal("147840.00"), response.getBudget());
    }

    @Test
    void calculatesSheetMetrics() {
        SheetToolRequest request = new SheetToolRequest();
        request.setRoomLength(6.8);
        request.setRoomWidth(5.2);
        request.setReservePercent(10.0);

        SheetToolResponse response = quickToolService.calculateSheet(request);

        assertEquals(35.36, response.getArea(), 0.001);
        assertEquals(38.90, response.getActualArea(), 0.001);
        assertEquals(13, response.getSheets());
        assertEquals(new BigDecimal("11570.00"), response.getBudget());
    }

    @Test
    void calculatesInsulationMetrics() {
        InsulationToolRequest request = new InsulationToolRequest();
        request.setArea(124.0);
        request.setThicknessMm(150.0);
        request.setPackVolume(0.6);

        InsulationToolResponse response = quickToolService.calculateInsulation(request);

        assertEquals(18.6, response.getVolume(), 0.001);
        assertEquals(31, response.getPacks());
        assertEquals(new BigDecimal("97650.00"), response.getBudget());
    }
}
