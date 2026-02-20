package staryhroft.templog.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import staryhroft.templog.dto.WeatherResponseDto;
import staryhroft.templog.exception.external.WeatherApiCityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherApiIntegrationTest {

    @Mock
    private RestTemplate restTemplate;

    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_API_URL = "http://test.api.com/weather";

    private WeatherApiIntegration weatherApiIntegration;

    @BeforeEach
    void setUp() {
        weatherApiIntegration = new WeatherApiIntegration(restTemplate, TEST_API_KEY, TEST_API_URL);
    }

    //когда от АПИ возвращается правильный ответ
    @Test
    void getCurrentTemperature_shouldReturnTemperature_whenApiReturnsValidResponse() {
        // given
        String cityName = "Moscow";
        WeatherResponseDto.Main main = new WeatherResponseDto.Main();
        main.setTemp(5.2);
        WeatherResponseDto responseDto = new WeatherResponseDto();
        responseDto.setMain(main);

        when(restTemplate.getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenReturn(responseDto);

        // when
        Double result = weatherApiIntegration.getCurrentTemperature(cityName);

        // then
        assertThat(result).isEqualTo(5.2);

        verify(restTemplate, times(1)).getForObject(anyString(), eq(WeatherResponseDto.class));
    }

    //когда от АПИ возвращается null
    @Test
    void getCurrentTemperature_shouldThrowException_whenRestTemplateReturnsNull() {
        // given
        String cityName = "Moscow";
        when(restTemplate.getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenReturn(null);

        // when / then
        assertThatThrownBy(() -> weatherApiIntegration.getCurrentTemperature(cityName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ошибка получения данных от погодного API");

        verify(restTemplate).getForObject(anyString(), eq(WeatherResponseDto.class));
    }
    //когда от АПИ возвращается DTO, но поле main = null
    @Test
    void getCurrentTemperature_shouldThrowException_whenMainIsNull() {
        // given
        String cityName = "Moscow";
        WeatherResponseDto responseDto = new WeatherResponseDto();
        responseDto.setMain(null);

        when(restTemplate.getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenReturn(responseDto);

        // when / then:
        assertThatThrownBy(() -> weatherApiIntegration.getCurrentTemperature(cityName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ошибка получения данных от погодного API");

        verify(restTemplate).getForObject(anyString(), eq(WeatherResponseDto.class));
    }

    //когда города с таким именем нет в АПИ
    @Test
    void getCurrentTemperature_shouldThrowWeatherApiCityNotFoundException_whenApiReturns404() {
        // given
        String cityName = "NonExistentCity";
        when(restTemplate.getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        // when / then
        assertThatThrownBy(() -> weatherApiIntegration.getCurrentTemperature(cityName))
                .isInstanceOf(WeatherApiCityNotFoundException.class)
                .hasMessageContaining(cityName);

        verify(restTemplate).getForObject(anyString(), eq(WeatherResponseDto.class));
    }

    //когда ошибка авторизации
    @Test
    void getCurrentTemperature_shouldThrowRuntimeException_whenOtherClientErrorOccurs() {
        // given
        String cityName = "Moscow";
        when(restTemplate.getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenThrow(HttpClientErrorException.Unauthorized.class);

        // when / then
        assertThatThrownBy(() -> weatherApiIntegration.getCurrentTemperature(cityName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ошибка соединения с погодным сервисом");

        verify(restTemplate).getForObject(anyString(), eq(WeatherResponseDto.class));
    }

    //когда ошибка сервера 500
    @Test
    void getCurrentTemperature_shouldThrowRuntimeException_whenServerErrorOccurs() {
        // given:
        String cityName = "Moscow";
        when(restTemplate.getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenThrow(HttpServerErrorException.InternalServerError.class);

        // when / then:
        assertThatThrownBy(() -> weatherApiIntegration.getCurrentTemperature(cityName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ошибка соединения с погодным сервисом");

        verify(restTemplate).getForObject(anyString(), eq(WeatherResponseDto.class));
    }

    //когда превышено время ожидания
    @Test
    void getCurrentTemperature_shouldThrowRuntimeException_whenResourceAccessExceptionOccurs() {
        // given
        String cityName = "Moscow";
        when(restTemplate.getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenThrow(new ResourceAccessException("Connection timeout"));

        // when / then
        assertThatThrownBy(() -> weatherApiIntegration.getCurrentTemperature(cityName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ошибка соединения с погодным сервисом");

        verify(restTemplate).getForObject(anyString(), eq(WeatherResponseDto.class));
    }

    //проверка правильного составления Url
    @Test
    void getCurrentTemperature_shouldBuildCorrectUrl() {
        // given
        String cityName = "Saint Petersburg";
        WeatherResponseDto.Main main = new WeatherResponseDto.Main();
        main.setTemp(3.8);
        WeatherResponseDto responseDto = new WeatherResponseDto();
        responseDto.setMain(main);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        when(restTemplate.getForObject(urlCaptor.capture(), eq(WeatherResponseDto.class)))
                .thenReturn(responseDto);

        // when
        weatherApiIntegration.getCurrentTemperature(cityName);

        // then
        String capturedUrl = urlCaptor.getValue();

        assertThat(capturedUrl).startsWith(TEST_API_URL);
        assertThat(capturedUrl).contains("q=" + cityName);
        assertThat(capturedUrl).contains("appid=" + TEST_API_KEY);
        assertThat(capturedUrl).contains("units=metric");
    }
}