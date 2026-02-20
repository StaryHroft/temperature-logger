package staryhroft.templog.service.city;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.enums.FavoriteStatus;
import staryhroft.templog.repository.CityRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityFinderTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityFinder cityFinder;

    //возвращается ли город, если он существует в БД
    @Test
    void findOrCreate_shouldReturnExistingCity_whenCityExists() {
        //given
        String cityName = "Moscow";

        City existingCity = City.builder()
                .id(1L)
                .name(cityName)
                .favoriteStatus(FavoriteStatus.NOT_FAVORITE)
                .build();

        when(cityRepository.findByName(cityName)).thenReturn(Optional.of(existingCity));

        //when
        City result = cityFinder.findOrCreate(cityName);

        //then
        assertThat(result).isSameAs(existingCity);

        verify(cityRepository, never()).save(any(City.class));
    }

    //создается ли город, если он не существует в БД
    @Test
    void findOrCreate_shouldCreateAndSaveNewCity_whenCityNotFound(){
        //given
        String cityName = "Saint Petersburg";

        when(cityRepository.findByName(cityName)).thenReturn(Optional.empty());

        //when
        City result = cityFinder.findOrCreate(cityName);

        //then
        assertThat(result.getName()).isEqualTo(cityName);
        assertThat(result.getFavoriteStatus()).isEqualTo(FavoriteStatus.NOT_FAVORITE);
        assertThat(result.getId()).isNull();

        verify(cityRepository, times(1)).findByName(cityName);
        verify(cityRepository, times(1)).save(any(City.class));
    }

    //создается ли город с корректными полями
    @Test
    void findOrCreate_shouldCreateNewCityWithCorrectFields_whenCityNotFound(){
        //given
        String cityName = "London";

        when(cityRepository.findByName(cityName)).thenReturn(Optional.empty());
        ArgumentCaptor<City> cityCaptor = ArgumentCaptor.forClass(City.class);

        //when
        City result = cityFinder.findOrCreate(cityName);

        //then
        verify(cityRepository).findByName(cityName);
        verify(cityRepository).save(cityCaptor.capture());

        City savedCity = cityCaptor.getValue();
        assertThat(savedCity.getName()).isEqualTo(cityName);
        assertThat(savedCity.getFavoriteStatus()).isEqualTo(FavoriteStatus.NOT_FAVORITE);
        assertThat(result).isSameAs(savedCity);

    }


}