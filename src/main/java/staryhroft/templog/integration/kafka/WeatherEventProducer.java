package staryhroft.templog.integration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import staryhroft.templog.event.WeatherUpdateEvent;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherEventProducer {
    private static final String TOPIC = "weather-update-requests";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishWeatherUpdate(String cityName) {
        WeatherUpdateEvent event = WeatherUpdateEvent.builder()
                .cityName(cityName)
                .requestedAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send(TOPIC, cityName, event);
        log.info("Published weather update event for city: {}", cityName);
    }
}
