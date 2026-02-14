package staryhroft.templog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CityListViewDto {
    private String name;
    private boolean favorite;
}
