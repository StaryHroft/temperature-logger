package staryhroft.templog.service.city;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.CityTemperature;
import staryhroft.templog.client.WeatherApiIntegration;
import staryhroft.templog.repository.CityTemperatureRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityTempetratureUpdaterTest {

    @Mock
    private CityTemperatureRepository temperatureRepository;

    @Mock
    private WeatherApiIntegration weatherApiIntegration;

    @InjectMocks
    private CityTemperatureUpdater temperatureUpdater;

    //возвращает актуальную температуру при ее наличии
    @Test
    void getFreshTemperature_shouldReturnExistingTemperature_whenItIsRecent() {
        // given:
        City city = City.builder()
                .id(1L)
                .name("Moscow")
                .build();

        LocalDateTime recentTime = LocalDateTime.now().minusHours(2);
        CityTemperature lastTemp = CityTemperature.builder()
                .city(city)
                .temperature(5.2)
                .timestamp(recentTime)
                .build();

        when(temperatureRepository.findFirstByCityOrderByTimestampDesc(city))
                .thenReturn(Optional.of(lastTemp));

        // when
        Double result = temperatureUpdater.getFreshTemperature(city);

        // then:
        assertThat(result).isEqualTo(5.2);

        verify(temperatureRepository, times(1)).findFirstByCityOrderByTimestampDesc(city);
        verify(weatherApiIntegration, never()).getCurrentTemperature(anyString());
        verify(temperatureRepository, never()).save(any(CityTemperature.class));
    }

    //вызывается ли АПИ при устаревшей температуре
    @Test
    void getFreshTemperature_shouldCallApiAndSave_whenExistingRecordIsOutdated() {
        // given
        City city = City.builder()
                .id(3L)
                .name("London")
                .build();

        LocalDateTime oldTime = LocalDateTime.now().minusHours(25);
        CityTemperature oldTemp = CityTemperature.builder()
                .city(city)
                .temperature(10.0)
                .timestamp(oldTime)
                .build();

        when(temperatureRepository.findFirstByCityOrderByTimestampDesc(city))
                .thenReturn(Optional.of(oldTemp));

        when(weatherApiIntegration.getCurrentTemperature(city.getName()))
                .thenReturn(11.5);

        // when
        Double result = temperatureUpdater.getFreshTemperature(city);

        // then
        assertThat(result).isEqualTo(11.5);

        verify(temperatureRepository).findFirstByCityOrderByTimestampDesc(city);
        verify(weatherApiIntegration).getCurrentTemperature(city.getName());

        ArgumentCaptor<CityTemperature> temperatureCaptor = ArgumentCaptor.forClass(CityTemperature.class);
        verify(temperatureRepository).save(temperatureCaptor.capture());

        CityTemperature saved = temperatureCaptor.getValue();
        assertThat(saved.getCity()).isSameAs(city);
        assertThat(saved.getTemperature()).isEqualTo(11.5);
    }

    //возвращается ли последняя запись
    @Test
    void getLatestRecord_shouldReturnLastRecord_whenExists() {
        // given
        City city = City.builder()
                .id(4L)
                .name("Paris")
                .build();

        CityTemperature lastTemp = CityTemperature.builder()
                .city(city)
                .temperature(7.2)
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();

        when(temperatureRepository.findFirstByCityOrderByTimestampDesc(city))
                .thenReturn(Optional.of(lastTemp));

        // when
        CityTemperature result = temperatureUpdater.getLatestRecord(city);

        // then
        assertThat(result).isSameAs(lastTemp);
        verify(temperatureRepository).findFirstByCityOrderByTimestampDesc(city);
    }
}