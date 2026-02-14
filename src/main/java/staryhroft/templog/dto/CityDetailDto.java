package staryhroft.templog.dto;

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
    private LocalDateTime lastTemperatureTime;
}
