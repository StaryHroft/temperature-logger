package staryhroft.templog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import staryhroft.templog.entity.enums.FavoriteStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CityDetailDto {
    private String name;
    private FavoriteStatus favoriteStatus;
    private Double lastTemperature;
    @JsonFormat(pattern = "HH:mm:ss dd:MM:yyyy")
    private LocalDateTime lastTemperatureTime;
}
