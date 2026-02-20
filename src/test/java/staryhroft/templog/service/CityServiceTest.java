package staryhroft.templog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import staryhroft.templog.dto.ApiResponse;
import staryhroft.templog.dto.CityDetailDto;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.CityTemperature;
import staryhroft.templog.entity.enums.FavoriteStatus;
import staryhroft.templog.exception.business.CityNotFoundException;
import staryhroft.templog.repository.CityRepository;
import staryhroft.templog.repository.CityTemperatureRepository;
import staryhroft.templog.service.city.CityFinder;
import staryhroft.templog.service.city.CityTemperatureUpdater;
import staryhroft.templog.service.favorite.FavoriteManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CityTemperatureRepository temperatureRepository;

    @Mock
    private CityFinder cityFinder;

    @Mock
    private CityTemperatureUpdater tempetratureUpdater;

    @Mock
    private FavoriteManager favoriteManager;

    @InjectMocks
    private CityService cityService;

    private City moscow;
    private City spb;
    private CityTemperature moscowTemp;
    private CityTemperature spbTemp;

    @BeforeEach
    void setUp() {
        moscow = City.builder()
                .id(1L)
                .name("Moscow")
                .favoriteStatus(FavoriteStatus.FAVORITE)
                .build();

        spb = City.builder()
                .id(2L)
                .name("Saint Petersburg")
                .favoriteStatus(FavoriteStatus.NOT_FAVORITE)
                .build();

        moscowTemp = CityTemperature.builder()
                .city(moscow)
                .temperature(5.2)
                .timestamp(LocalDateTime.now().minusHours(5))
                .build();

        spbTemp = CityTemperature.builder()
                .city(spb)
                .temperature(3.8)
                .timestamp(LocalDateTime.now().minusHours(5))
                .build();
    }

    //получается ли отсортированный список
    @Test
    void getAllCities_shouldReturnSortedListWithTemperatures() {
        //given
        List<City> favorites = List.of(moscow);
        List<City> others = List.of(spb);

        when(cityRepository.findByFavoriteStatusOrderByIdDesc(FavoriteStatus.FAVORITE))
                .thenReturn(favorites);
        when(cityRepository.findByFavoriteStatusOrderByIdDesc(FavoriteStatus.NOT_FAVORITE))
                .thenReturn(others);
        when(temperatureRepository.findFirstByCityOrderByTimestampDesc(moscow))
                .thenReturn(Optional.of(moscowTemp));
        when(temperatureRepository.findFirstByCityOrderByTimestampDesc(spb))
                .thenReturn(Optional.of(spbTemp));

        //when
        ApiResponse<List<CityDetailDto>> response = cityService.getAllCities();
        List<CityDetailDto> result = response.getData();
        String message = response.getMessage();

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Moscow");
        assertThat(result.get(0).getFavoriteStatus()).isEqualTo(FavoriteStatus.FAVORITE);
        assertThat(result.get(0).getLastTemperature()).isEqualTo(5.2);
        assertThat(result.get(1).getName()).isEqualTo("Saint Petersburg");
        assertThat(result.get(1).getFavoriteStatus()).isEqualTo(FavoriteStatus.NOT_FAVORITE);
        assertThat(result.get(1).getLastTemperature()).isEqualTo(3.8);
        assertThat(message).isNull();// т.к. список уже пуст

        verify(cityRepository).findByFavoriteStatusOrderByIdDesc(FavoriteStatus.FAVORITE);
        verify(cityRepository).findByFavoriteStatusOrderByIdDesc(FavoriteStatus.NOT_FAVORITE);
        verify(temperatureRepository, times(2))
                .findFirstByCityOrderByTimestampDesc(any(City.class));
    }

    //возвращается ли валидное сообщение, если список городов пуст
    @Test
    void getAllCities_whenNoCities_shouldReturnEmptyListWithMessage() {
        //given
        when(cityRepository.findByFavoriteStatusOrderByIdDesc(FavoriteStatus.FAVORITE))
                .thenReturn(List.of());
        when(cityRepository.findByFavoriteStatusOrderByIdDesc(FavoriteStatus.NOT_FAVORITE))
                .thenReturn(List.of());

        //when
        ApiResponse<List<CityDetailDto>> response = cityService.getAllCities();
        List<CityDetailDto> result = response.getData();
        String message = response.getMessage();

        //then
        assertThat(result).isEmpty();
        assertThat(message).isEqualTo("Список городов пуст");
    }

    //удаление города если он есть
    @Test
    void deleteCity_shouldDeleteCity_whenCityExists() {
        //given
        String cityName = "Moscow";
        City existingCity = City.builder()
                .id(1L)
                .name(cityName)
                .favoriteStatus(FavoriteStatus.NOT_FAVORITE)
                .build();

        when(cityRepository.findByName(cityName)).thenReturn(Optional.of(existingCity));

        //when
        cityService.deleteCity(cityName);

        //then
        verify(cityRepository, times(1)).findByName(cityName);
        verify(cityRepository, times(1)).delete(existingCity);
        verifyNoMoreInteractions(cityRepository);

    }

    //удаление города если его нет
    @Test
    void deleteCity_shouldThrowCityNotFoundException_whenCityDoesNotExist() {
        //given
        String cityName = "NonExistentCity";
        when(cityRepository.findByName(cityName)).thenReturn(Optional.empty());

        //when / then
        assertThatThrownBy(() -> cityService.deleteCity(cityName))
                .isInstanceOf(CityNotFoundException.class)
                .hasMessageContaining(cityName);

        verify(cityRepository, times(1)).findByName(cityName);
        verify(cityRepository, never()).delete(any(City.class));
        verifyNoMoreInteractions(cityRepository);

    }


}
