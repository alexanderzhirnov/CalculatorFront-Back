package backend.back.service;

import backend.back.dto.request.CalculationRequest;
import backend.back.dto.response.CalculationResponse;
import backend.back.entity.Calculation;
import backend.back.entity.CalculationElement;
import backend.back.entity.CalculationResultItem;
import backend.back.entity.Client;
import backend.back.entity.User;
import backend.back.entity.enums.CalculationStatus;
import backend.back.exception.ResourceNotFoundException;
import backend.back.exception.UnauthorizedActionException;
import backend.back.exception.ValidationException;
import backend.back.mapper.CalculationMapper;
import backend.back.repository.CalculationRepository;
import backend.back.repository.ClientRepository;
import backend.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationService {

    private final CalculationRepository calculationRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final CalculationMapper calculationMapper;

    @Transactional(readOnly = true)
    public List<CalculationResponse> getByClientId(Long clientId) {
        User user = currentUser();
        clientRepository.findByIdAndCreatedBy(clientId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Клиент", clientId));

        return calculationRepository.findAllByClientIdAndClientCreatedByOrderByCreatedAtDesc(clientId, user).stream()
                .map(calculationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CalculationResponse getById(Long id) {
        return calculationMapper.toResponse(findById(id));
    }

    @Transactional
    public CalculationResponse create(Long clientId, CalculationRequest request) {
        Calculation calculation = new Calculation();
        calculation.setClient(findClientById(clientId));
        calculation.setCreatedBy(currentUser());
        calculation.setConstructionAddress(normalizeAddress(request.getConstructionAddress()));
        calculation.setStatus(CalculationStatus.ACTUAL);
        calculation.setCreatedAt(LocalDateTime.now());
        calculation.setUpdatedAt(LocalDateTime.now());
        calculation.setPricesFixedUntil(LocalDateTime.now().plusDays(10));
        return calculationMapper.toResponse(calculationRepository.save(calculation));
    }

    @Transactional
    public CalculationResponse updateStatus(Long id, CalculationStatus newStatus) {
        Calculation calculation = findById(id);
        if (calculation.getStatus() == CalculationStatus.CONTRACT_SIGNED) {
            throw new UnauthorizedActionException(
                    "Нельзя изменить статус расчёта с заключённым договором");
        }

        calculation.setStatus(newStatus);
        calculation.setUpdatedAt(LocalDateTime.now());
        if (newStatus == CalculationStatus.ACTUAL) {
            calculation.setPricesFixedUntil(LocalDateTime.now().plusDays(10));
        }

        return calculationMapper.toResponse(calculationRepository.save(calculation));
    }

    @Transactional
    public CalculationResponse copy(Long id) {
        Calculation original = findById(id);
        Calculation copy = new Calculation();
        copy.setClient(original.getClient());
        copy.setCreatedBy(currentUser());
        copy.setConstructionAddress(original.getConstructionAddress());
        copy.setStatus(CalculationStatus.ACTUAL);
        copy.setCreatedAt(LocalDateTime.now());
        copy.setUpdatedAt(LocalDateTime.now());
        copy.setPricesFixedUntil(LocalDateTime.now().plusDays(10));

        List<CalculationElement> copiedElements = new ArrayList<>();
        for (CalculationElement element : original.getElements()) {
            CalculationElement copied = new CalculationElement();
            copied.setCalculation(copy);
            copied.setElementType(element.getElementType());
            copied.setInputParams(element.getInputParams());
            copied.setCreatedAt(LocalDateTime.now());
            copied.setUpdatedAt(LocalDateTime.now());

            List<CalculationResultItem> copiedItems = new ArrayList<>();
            for (CalculationResultItem item : element.getResultItems()) {
                CalculationResultItem copiedItem = new CalculationResultItem();
                copiedItem.setCalculationElement(copied);
                copiedItem.setMaterialName(item.getMaterialName());
                copiedItem.setUnit(item.getUnit());
                copiedItem.setQuantity(item.getQuantity());
                copiedItem.setUnitPrice(item.getUnitPrice());
                copiedItem.setTotalPrice(item.getTotalPrice());
                copiedItem.setSection(item.getSection());
                copiedItems.add(copiedItem);
            }

            copied.setResultItems(copiedItems);
            copiedElements.add(copied);
        }
        copy.setElements(copiedElements);

        return calculationMapper.toResponse(calculationRepository.save(copy));
    }

    @Transactional
    public void delete(Long id) {
        Calculation calculation = findById(id);
        if (calculation.getStatus() == CalculationStatus.CONTRACT_SIGNED) {
            throw new UnauthorizedActionException(
                    "Нельзя удалить расчёт с заключённым договором");
        }
        calculationRepository.delete(calculation);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOutdatedCalculations() {
        List<Calculation> expired = calculationRepository.findAllByStatusAndPricesFixedUntilBefore(
                CalculationStatus.ACTUAL,
                LocalDateTime.now());
        expired.forEach(calculation -> calculation.setStatus(CalculationStatus.NOT_ACTUAL));
        calculationRepository.saveAll(expired);
        log.info("Переведено в NOT_ACTUAL расчётов: {}", expired.size());
    }

    private Calculation findById(Long id) {
        return calculationRepository.findByIdAndClientCreatedBy(id, currentUser())
                .orElseThrow(() -> new ResourceNotFoundException("Расчёт", id));
    }

    private Client findClientById(Long id) {
        return clientRepository.findByIdAndCreatedBy(id, currentUser())
                .orElseThrow(() -> new ResourceNotFoundException("Клиент", id));
    }

    private User currentUser() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLoginIgnoreCase(login).orElseThrow();
    }

    private String normalizeAddress(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new ValidationException("Адрес объекта обязателен");
        }
        if (normalized.length() < 5 || normalized.length() > 255) {
            throw new ValidationException("Адрес объекта должен быть от 5 до 255 символов");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replaceAll("\\s{2,}", " ");
        return normalized.isEmpty() ? null : normalized;
    }
}
