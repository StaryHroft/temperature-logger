package staryhroft.templog.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import staryhroft.templog.client.WeatherApiIntegration;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.CityTemperature;
import staryhroft.templog.event.WeatherUpdateEvent;
import staryhroft.templog.exception.business.CityNotFoundException;
import staryhroft.templog.exception.external.WeatherApiCityNotFoundException;
import staryhroft.templog.repository.CityRepository;
import staryhroft.templog.repository.CityTemperatureRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherUpdateConsumer {
    private final CityRepository cityRepository;
    private final WeatherApiIntegration weatherApiIntegration;
    private final CityTemperatureRepository temperatureRepository;

    @KafkaListener(topics = "weather-update-requests", groupId = "weather-group")
    @Transactional
    public void handleWeatherUpdate(WeatherUpdateEvent event) {
        String cityName = event.getCityName();
        log.info("Получено событие обновления погоды для города: {}", cityName);

        try {
            // 1. Найти город в БД
            City city = cityRepository.findByName(cityName)
                    .orElseThrow(() -> new CityNotFoundException("Город не найден: " + cityName));

            // 2. Запросить текущую температуру из внешнего API
            Double newTemperature = weatherApiIntegration.getCurrentTemperature(cityName);

            // 3. Сохранить новую запись температуры
            CityTemperature newRecord = CityTemperature.builder()
                    .city(city)
                    .temperature(newTemperature)
                    .build();
            temperatureRepository.save(newRecord);

            log.info("Температура для города {} успешно обновлена: {}", cityName, newTemperature);

        } catch (CityNotFoundException e) {
            // Город удалили после публикации события – просто логируем и игнорируем
            log.warn("Город {} не найден в БД, возможно, был удалён", cityName);
        } catch (WeatherApiCityNotFoundException e) {
            // Город не найден во внешнем API – логируем, возможно, позже удалим его из БД?
            log.warn("Город {} не найден в погодном API", cityName);
        } catch (Exception e) {
            // Другие ошибки (сеть, таймаут и т.п.) – логируем, можно повторить позже через ретраи
            log.error("Ошибка при обновлении погоды для города {}: {}", cityName, e.getMessage(), e);
        }
    }
}
