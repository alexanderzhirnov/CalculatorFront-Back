package backend.back.controller;

import backend.back.dto.request.SupportRequest;
import backend.back.dto.response.SupportResponse;
import backend.back.service.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@Tag(name = "Поддержка")
public class SupportController {

    private final SupportService supportService;

    @PostMapping("/requests")
    @Operation(summary = "Отправить обращение в поддержку")
    public ResponseEntity<SupportResponse> submit(@Valid @RequestBody SupportRequest request) {
        return ResponseEntity.ok(supportService.submit(request));
    }
}
