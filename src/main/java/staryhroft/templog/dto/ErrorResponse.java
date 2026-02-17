package staryhroft.templog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    @JsonFormat(pattern = "HH:mm:ss dd:MM:yyyy")
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String title;
    private String message;
}
