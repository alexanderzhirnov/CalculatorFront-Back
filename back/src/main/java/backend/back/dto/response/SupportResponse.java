package backend.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SupportResponse {
    private boolean accepted;
    private String transport;
}
