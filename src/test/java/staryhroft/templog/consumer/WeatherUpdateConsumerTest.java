package staryhroft.templog.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.CityTemperature;
import staryhroft.templog.entity.enums.FavoriteStatus;
import staryhroft.templog.event.WeatherUpdateEvent;
import staryhroft.templog.exception.business.CityNotFoundException;
import staryhroft.templog.exception.external.WeatherApiCityNotFoundException;
import staryhroft.templog.client.WeatherApiIntegration;
import staryhroft.templog.repository.CityRepository;
import staryhroft.templog.repository.CityTemperatureRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherUpdateConsumerTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private WeatherApiIntegration weatherApiIntegration;

    @Mock
    private CityTemperatureRepository temperatureRepository;

    @InjectMocks
    private WeatherUpdateConsumer consumer;

    private City city;
    private WeatherUpdateEvent event;

    @BeforeEach
    void setUp() {
        city = City.builder()
                .id(1L)
                .name("Moscow")
                .favoriteStatus(FavoriteStatus.NOT_FAVORITE)
                .build();

        event = WeatherUpdateEvent.builder()
                .cityName("Moscow")
                .requestedAt(LocalDateTime.now())
                .build();
    }
    //обновляет температуру
    @Test
    void handleWeatherUpdate_shouldUpdateTemperature_whenCityExistsAndApiReturnsTemperature() {
        // given
        when(cityRepository.findByName("Moscow")).thenReturn(Optional.of(city));
        when(weatherApiIntegration.getCurrentTemperature("Moscow")).thenReturn(15.5);

        // Настраиваем мок репозитория, чтобы он возвращал тот же объект, который получил
        when(temperatureRepository.save(any(CityTemperature.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        consumer.handleWeatherUpdate(event);

        // then
        verify(cityRepository).findByName("Moscow");
        verify(weatherApiIntegration).getCurrentTemperature("Moscow");

        ArgumentCaptor<CityTemperature> temperatureCaptor = ArgumentCaptor.forClass(CityTemperature.class);
        verify(temperatureRepository).save(temperatureCaptor.capture());

        CityTemperature saved = temperatureCaptor.getValue();
        assertThat(saved).isNotNull();
        assertThat(saved.getCity()).isEqualTo(city);
        assertThat(saved.getTemperature()).isEqualTo(15.5);
    }

    @Test
    void handleWeatherUpdate_shouldLogWarningAndExit_whenCityNotFound() {
        // given
        when(cityRepository.findByName("Moscow")).thenReturn(Optional.empty());

        // when
        consumer.handleWeatherUpdate(event);

        // then
        verify(cityRepository).findByName("Moscow");
        verify(weatherApiIntegration, never()).getCurrentTemperature(anyString());
        verify(temperatureRepository, never()).save(any());
        // Проверяем, что исключение не пробросилось (метод просто залогировал)
    }
    //фиксирует ошибку и выходит если город не найден
    @Test
    void handleWeatherUpdate_shouldLogWarningAndExit_whenCityNotFoundInApi() {
        // given
        when(cityRepository.findByName("Moscow")).thenReturn(Optional.of(city));
        when(weatherApiIntegration.getCurrentTemperature("Moscow"))
                .thenThrow(new WeatherApiCityNotFoundException("City not found"));

        // when
        consumer.handleWeatherUpdate(event);

        // then
        verify(cityRepository).findByName("Moscow");
        verify(weatherApiIntegration).getCurrentTemperature("Moscow");
        verify(temperatureRepository, never()).save(any());
    }
    //фиксирует ошибку и выходит
    @Test
    void handleWeatherUpdate_shouldLogErrorAndExit_whenOtherExceptionOccurs() {
        // given
        when(cityRepository.findByName("Moscow")).thenReturn(Optional.of(city));
        when(weatherApiIntegration.getCurrentTemperature("Moscow"))
                .thenThrow(new RuntimeException("Connection timeout"));

        // when
        consumer.handleWeatherUpdate(event);

        // then
        verify(cityRepository).findByName("Moscow");
        verify(weatherApiIntegration).getCurrentTemperature("Moscow");
        verify(temperatureRepository, never()).save(any());
        // Проверяем, что исключение не пробросилось наружу (метод обработал)
    }
}
