package staryhroft.templog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponseDto {
    private Main main;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main{
        private Double temp;
    }

}
