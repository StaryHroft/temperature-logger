package staryhroft.templog.service.city;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.enums.FavoriteStatus;
import staryhroft.templog.repository.CityRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class CityFinder {
    private final CityRepository cityRepository;

    public City findOrCreate(String cityName){
        return cityRepository.findByName(cityName)
                .orElseGet(() -> {
                    City newCity = City.builder()
                            .name(cityName)
                            .favoriteStatus(FavoriteStatus.NOT_FAVORITE)
                            .build();
                    cityRepository.save(newCity);
                    log.info("Создан новый город: {}", cityName);
                    return newCity;
                });
    }
}
