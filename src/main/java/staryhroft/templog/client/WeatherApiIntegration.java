package staryhroft.templog.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import staryhroft.templog.dto.WeatherResponseDto;
import staryhroft.templog.exception.business.WeatherApiCityNotFoundException;

@Component
@Slf4j
public class WeatherApiIntegration {
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;

    public WeatherApiIntegration(RestTemplate restTemplate,
                                 @Value("${weather.api.key}") String apiKey,
                                 @Value("${weather.api.url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public Double getCurrentTemperature(String cityName) {
        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("q", cityName)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build()
                .toUriString();

        try {
            WeatherResponseDto response = restTemplate
                    .getForObject(url, WeatherResponseDto.class);
            if (response != null && response.getMain() != null) {
                return response.getMain().getTemp();
            } else {
                log.error("Не удалось получить температуру для города {}: пустой ответ", cityName);
                throw new RuntimeException("Ошибка получения данных от погодного API");
            }
        } catch (HttpClientErrorException.NotFound e){
            log.warn("Город {} не найден в погодном API", cityName);
            throw new WeatherApiCityNotFoundException(cityName);
        } catch (RestClientException e) {
            log.error("Ошибка при запросе погоды для города {}: {}", cityName, e.getMessage());
            throw new RuntimeException("Ошибка соединения с погодным сервисом", e);
        }
    }
}
