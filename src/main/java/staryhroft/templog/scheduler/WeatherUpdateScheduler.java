package staryhroft.templog.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import staryhroft.templog.service.weather.WeatherUpdateService;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherUpdateScheduler {
    private final WeatherUpdateService weatherUpdateService;

    @Scheduled(cron = "0 0 */6 * * *", zone = "Europe/Moscow")
    public void updateWeather(){

        log.info("Начало планового обновления погоды");
        weatherUpdateService.updateWeatherForAllCities();
        log.info("Плановое обновление погоды завершено");
    }
}
