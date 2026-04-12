package backend.back.service;

import backend.back.dto.request.ClientRequest;
import backend.back.dto.response.ClientResponse;
import backend.back.entity.Client;
import backend.back.entity.User;
import backend.back.exception.ResourceNotFoundException;
import backend.back.exception.ValidationException;
import backend.back.mapper.ClientMapper;
import backend.back.repository.ClientRepository;
import backend.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ClientService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9()\\-\\s]+$");

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientMapper clientMapper;

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAllByCreatedByOrderByCreatedAtDesc(currentUser()).stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse getById(Long id) {
        return clientMapper.toResponse(findById(id));
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        Client client = new Client();
        fillClient(client, request);
        client.setCreatedBy(currentUser());
        client.setCreatedAt(LocalDateTime.now());
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = findById(id);
        fillClient(client, request);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public void delete(Long id) {
        clientRepository.delete(findById(id));
    }

    private void fillClient(Client client, ClientRequest request) {
        client.setLastName(normalizeName(request.getLastName(), "Фамилия обязательна", "Фамилия"));
        client.setFirstName(normalizeName(request.getFirstName(), "Имя обязательно", "Имя"));
        client.setPatronymic(normalizeOptional(request.getPatronymic()));
        client.setPhone(normalizePhone(request.getPhone()));
        client.setEmail(normalizeEmail(request.getEmail()));
        client.setAddress(normalizeAddress(request.getAddress()));
    }

    private Client findById(Long id) {
        return clientRepository.findByIdAndCreatedBy(id, currentUser())
                .orElseThrow(() -> new ResourceNotFoundException("Клиент", id));
    }

    private User currentUser() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLoginIgnoreCase(login).orElseThrow();
    }

    private String normalizeName(String value, String requiredMessage, String fieldLabel) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new ValidationException(requiredMessage);
        }
        if (normalized.length() > 100) {
            throw new ValidationException(fieldLabel + " не должно превышать 100 символов");
        }
        return normalized;
    }

    private String normalizePhone(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return null;
        }
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new ValidationException("Телефон может содержать только цифры, пробелы, скобки и дефис");
        }

        String digits = normalized.replaceAll("\\D", "");
        if (digits.length() < 10 || digits.length() > 15) {
            throw new ValidationException("Телефон должен содержать от 10 до 15 цифр");
        }
        return normalized;
    }

    private String normalizeEmail(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return null;
        }

        String email = normalized.toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Укажите корректный email клиента");
        }
        return email;
    }

    private String normalizeAddress(String value) {
        String normalized = normalizeOptional(value);
        if (normalized != null && normalized.length() > 255) {
            throw new ValidationException("Адрес не должен превышать 255 символов");
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
