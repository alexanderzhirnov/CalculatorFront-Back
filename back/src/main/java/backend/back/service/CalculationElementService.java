package backend.back.service;

import backend.back.dto.request.FoundationParamsRequest;
import backend.back.dto.request.FoundationPresetRequest;
import backend.back.dto.request.FrameParamsRequest;
import backend.back.dto.request.FramePresetRequest;
import backend.back.dto.response.CalculationElementResponse;
import backend.back.entity.Calculation;
import backend.back.entity.CalculationElement;
import backend.back.entity.CalculationResultItem;
import backend.back.entity.User;
import backend.back.entity.enums.CalculationStatus;
import backend.back.entity.enums.ElementType;
import backend.back.exception.ResourceNotFoundException;
import backend.back.exception.UnauthorizedActionException;
import backend.back.mapper.CalculationElementMapper;
import backend.back.repository.CalculationElementRepository;
import backend.back.repository.CalculationRepository;
import backend.back.repository.CalculationResultItemRepository;
import backend.back.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalculationElementService {

    private final CalculationRepository calculationRepository;
    private final CalculationElementRepository elementRepository;
    private final CalculationResultItemRepository resultItemRepository;
    private final CalculationEngineService engineService;
    private final CalculationPresetService presetService;
    private final CalculationElementMapper elementMapper;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Transactional
    public CalculationElementResponse addOrUpdateFrame(Long calculationId, FramePresetRequest request) {
        Calculation calculation = getCalculation(calculationId);
        checkEditable(calculation);

        FrameParamsRequest resolvedRequest = presetService.buildFrameRequest(request);
        List<CalculationResultItem> items = engineService.calculateFrame(resolvedRequest);
        return saveCalculatedElement(calculation, ElementType.FRAME, resolvedRequest, items);
    }

    @Transactional
    public CalculationElementResponse addOrUpdateFoundation(Long calculationId, FoundationPresetRequest request) {
        Calculation calculation = getCalculation(calculationId);
        checkEditable(calculation);

        FoundationParamsRequest resolvedRequest = presetService.buildFoundationRequest(request);
        List<CalculationResultItem> items = engineService.calculateFoundation(resolvedRequest);
        return saveCalculatedElement(calculation, ElementType.FOUNDATION, resolvedRequest, items);
    }

    private CalculationElementResponse saveCalculatedElement(
            Calculation calculation,
            ElementType elementType,
            Object resolvedRequest,
            List<CalculationResultItem> items) {
        CalculationElement element = elementRepository
                .findByCalculationIdAndElementType(calculation.getId(), elementType)
                .orElseGet(() -> {
                    CalculationElement created = new CalculationElement();
                    created.setCalculation(calculation);
                    created.setElementType(elementType);
                    created.setCreatedAt(LocalDateTime.now());
                    return created;
                });

        element.setInputParams(objectMapper.convertValue(resolvedRequest, Map.class));
        element.setUpdatedAt(LocalDateTime.now());
        CalculationElement saved = elementRepository.save(element);

        resultItemRepository.deleteAllByCalculationElementId(saved.getId());
        items.forEach(item -> item.setCalculationElement(saved));
        resultItemRepository.saveAll(items);
        saved.setResultItems(items);

        return elementMapper.toResponse(saved);
    }

    private Calculation getCalculation(Long id) {
        return calculationRepository.findByIdAndClientCreatedBy(id, currentUser())
                .orElseThrow(() -> new ResourceNotFoundException("Расчёт", id));
    }

    private void checkEditable(Calculation calculation) {
        if (calculation.getStatus() == CalculationStatus.CONTRACT_SIGNED) {
            throw new UnauthorizedActionException(
                    "Нельзя изменить элементы расчёта с заключённым договором");
        }
    }

    private User currentUser() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLoginIgnoreCase(login).orElseThrow();
    }
}
