package backend.back.service;

import backend.back.dto.request.SupportRequest;
import backend.back.dto.response.SupportResponse;
import backend.back.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${app.support.telegram-bot-token:}")
    private String telegramBotToken;

    @Value("${app.support.telegram-chat-id:}")
    private String telegramChatId;

    public SupportResponse submit(SupportRequest request) {
        SupportRequest normalized = normalize(request);
        String text = buildMessage(normalized);

        if (telegramBotToken.isBlank() || telegramChatId.isBlank()) {
            log.info("Support request accepted in log mode from '{}' [{} / {}]: {}",
                    normalized.getName(),
                    normalized.getSource(),
                    normalized.getPage(),
                    normalized.getMessage());
            return new SupportResponse(true, "log");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chat_id", telegramChatId);
        payload.put("text", text);
        payload.put("disable_web_page_preview", true);

        try {
            HttpRequest telegramRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.telegram.org/bot" + telegramBotToken + "/sendMessage"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(telegramRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode body = objectMapper.readTree(response.body());
            if (response.statusCode() / 100 != 2 || !body.path("ok").asBoolean(false)) {
                throw new ValidationException("Не удалось отправить сообщение в поддержку");
            }

            return new SupportResponse(true, "telegram");
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ValidationException("Не удалось отправить сообщение в поддержку");
        }
    }

    private SupportRequest normalize(SupportRequest request) {
        SupportRequest normalized = new SupportRequest();
        normalized.setName(normalizeRequired(request.getName(), "Имя обязательно"));
        normalized.setContact(normalizeRequired(request.getContact(), "Контакт обязателен"));
        normalized.setMessage(normalizeRequired(request.getMessage(), "Сообщение обязательно"));
        normalized.setSource(normalizeOptional(request.getSource(), "site"));
        normalized.setPage(normalizeOptional(request.getPage(), "home"));
        return normalized;
    }

    private String buildMessage(SupportRequest request) {
        return String.join("\n",
                "Запрос с сайта",
                "",
                "Имя: " + request.getName(),
                "Контакт: " + request.getContact(),
                "Источник: " + request.getSource(),
                "Страница: " + request.getPage(),
                "",
                "Сообщение:",
                request.getMessage());
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value, null);
        if (normalized == null) {
            throw new ValidationException(message);
        }
        return normalized;
    }

    private String normalizeOptional(String value, String fallback) {
        if (value == null) {
            return fallback;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? fallback : normalized;
    }
}
