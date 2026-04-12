package backend.back.service;

import backend.back.dto.request.ConcreteToolRequest;
import backend.back.dto.request.FacadeToolRequest;
import backend.back.dto.request.InsulationToolRequest;
import backend.back.dto.request.SheetToolRequest;
import backend.back.dto.response.ConcreteToolResponse;
import backend.back.dto.response.FacadeToolResponse;
import backend.back.dto.response.InsulationToolResponse;
import backend.back.dto.response.SheetToolResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class QuickToolService {

    private static final double FACADE_RESERVE_FACTOR = 1.08;
    private static final double CONCRETE_MIXER_CAPACITY = 7.0;
    private static final BigDecimal CONCRETE_PRICE_PER_CUBIC_METER = BigDecimal.valueOf(5600);
    private static final double SHEET_AREA = 3.125;
    private static final BigDecimal SHEET_PRICE = BigDecimal.valueOf(890);
    private static final BigDecimal INSULATION_PACK_PRICE = BigDecimal.valueOf(3150);

    public FacadeToolResponse calculateFacade(FacadeToolRequest request) {
        double area = request.getWidth() * request.getHeight();
        double reserveArea = area * FACADE_RESERVE_FACTOR;
        BigDecimal total = money(reserveArea * request.getPricePerSquareMeter());
        return new FacadeToolResponse(round(area), round(reserveArea), total);
    }

    public ConcreteToolResponse calculateConcrete(ConcreteToolRequest request) {
        double volume = request.getLength() * request.getWidth() * (request.getDepthCm() / 100.0);
        int mixers = Math.max(1, (int) Math.ceil(volume / CONCRETE_MIXER_CAPACITY));
        BigDecimal budget = money(volume).multiply(CONCRETE_PRICE_PER_CUBIC_METER).setScale(2, RoundingMode.HALF_UP);
        return new ConcreteToolResponse(round(volume), mixers, budget);
    }

    public SheetToolResponse calculateSheet(SheetToolRequest request) {
        double area = request.getRoomLength() * request.getRoomWidth();
        double actualArea = area * (1 + request.getReservePercent() / 100.0);
        int sheets = Math.max(1, (int) Math.ceil(actualArea / SHEET_AREA));
        BigDecimal budget = SHEET_PRICE.multiply(BigDecimal.valueOf(sheets)).setScale(2, RoundingMode.HALF_UP);
        return new SheetToolResponse(round(area), round(actualArea), sheets, budget);
    }

    public InsulationToolResponse calculateInsulation(InsulationToolRequest request) {
        double volume = request.getArea() * (request.getThicknessMm() / 1000.0);
        int packs = Math.max(1, (int) Math.ceil(volume / request.getPackVolume()));
        BigDecimal budget = INSULATION_PACK_PRICE.multiply(BigDecimal.valueOf(packs)).setScale(2, RoundingMode.HALF_UP);
        return new InsulationToolResponse(round(volume), packs, budget);
    }

    private BigDecimal money(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private double round(double value) {
        return money(value).doubleValue();
    }
}
