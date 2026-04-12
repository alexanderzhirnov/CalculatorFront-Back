package backend.back.service;

import backend.back.dto.request.SupportRequest;
import backend.back.dto.response.SupportResponse;
import backend.back.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupportServiceTest {

    private SupportService supportService;

    @BeforeEach
    void setUp() {
        supportService = new SupportService(new ObjectMapper());
        ReflectionTestUtils.setField(supportService, "telegramBotToken", "");
        ReflectionTestUtils.setField(supportService, "telegramChatId", "");
    }

    @Test
    void submitFallsBackToLogTransportWhenTelegramIsNotConfigured() {
        SupportRequest request = new SupportRequest();
        request.setName("  Smoke User  ");
        request.setContact("  @smoke  ");
        request.setMessage("  Нужен тестовый созвон  ");
        request.setSource("  landing  ");
        request.setPage("  home  ");

        SupportResponse response = supportService.submit(request);

        assertThat(response.isAccepted()).isTrue();
        assertThat(response.getTransport()).isEqualTo("log");
    }

    @Test
    void submitRejectsBlankRequiredFields() {
        SupportRequest request = new SupportRequest();
        request.setName("   ");
        request.setContact("+79990000000");
        request.setMessage("Привет");

        assertThatThrownBy(() -> supportService.submit(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Имя");
    }
}
