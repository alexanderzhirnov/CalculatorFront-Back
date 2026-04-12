package backend.back.controller;

import backend.back.dto.request.ConcreteToolRequest;
import backend.back.dto.request.FacadeToolRequest;
import backend.back.dto.request.InsulationToolRequest;
import backend.back.dto.request.SheetToolRequest;
import backend.back.dto.response.ConcreteToolResponse;
import backend.back.dto.response.FacadeToolResponse;
import backend.back.dto.response.InsulationToolResponse;
import backend.back.dto.response.SheetToolResponse;
import backend.back.service.QuickToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
@Tag(name = "Быстрые инструменты")
public class QuickToolController {

    private final QuickToolService quickToolService;

    @PostMapping("/facade")
    @Operation(summary = "Рассчитать фасадный виджет")
    public ResponseEntity<FacadeToolResponse> calculateFacade(@Valid @RequestBody FacadeToolRequest request) {
        return ResponseEntity.ok(quickToolService.calculateFacade(request));
    }

    @PostMapping("/concrete")
    @Operation(summary = "Рассчитать бетонный виджет")
    public ResponseEntity<ConcreteToolResponse> calculateConcrete(@Valid @RequestBody ConcreteToolRequest request) {
        return ResponseEntity.ok(quickToolService.calculateConcrete(request));
    }

    @PostMapping("/sheet")
    @Operation(summary = "Рассчитать виджет по листовым материалам")
    public ResponseEntity<SheetToolResponse> calculateSheet(@Valid @RequestBody SheetToolRequest request) {
        return ResponseEntity.ok(quickToolService.calculateSheet(request));
    }

    @PostMapping("/insulation")
    @Operation(summary = "Рассчитать виджет утепления")
    public ResponseEntity<InsulationToolResponse> calculateInsulation(@Valid @RequestBody InsulationToolRequest request) {
        return ResponseEntity.ok(quickToolService.calculateInsulation(request));
    }
}
