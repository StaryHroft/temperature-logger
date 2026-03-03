package staryhroft.templog.service.city;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import staryhroft.templog.client.WeatherApiIntegration;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.CityTemperature;
import staryhroft.templog.integration.kafka.WeatherEventProducer;
import staryhroft.templog.repository.CityTemperatureRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CityTemperatureUpdater {

    private final CityTemperatureRepository temperatureRepository;
    private final WeatherEventProducer weatherEventProducer;

    public Double getFreshTemperature(City city) {
        CityTemperature latest = getLatestRecord(city);

        if (isFresh(latest)) {
            log.debug("Температура для города {} актуальна", city.getName());
            return latest.getTemperature();
        }

        // Данные устарели – публикуем событие и возвращаем старую температуру
        log.info("Данные для города {} устарели, публикуем событие на обновление", city.getName());
        weatherEventProducer.publishWeatherUpdate(city.getName());
        return latest.getTemperature();
    }

    private boolean isFresh(CityTemperature record) {
        return record.getTimestamp().isAfter(LocalDateTime.now().minusDays(1));
    }

    public CityTemperature getLatestRecord(City city) {
        return temperatureRepository.findFirstByCityOrderByTimestampDesc(city)
                .orElseThrow(() -> new IllegalStateException("Температура не найдена для города " + city.getName()));
    }
}
