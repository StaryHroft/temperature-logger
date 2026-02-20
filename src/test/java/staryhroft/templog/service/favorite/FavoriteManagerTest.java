package staryhroft.templog.service.favorite;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.enums.FavoriteStatus;
import staryhroft.templog.exception.business.CityAlreadyFavoriteException;
import staryhroft.templog.exception.business.CityNotFavoriteException;
import staryhroft.templog.exception.business.FavoritesLimitExceededException;
import staryhroft.templog.repository.CityRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteManagerTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private FavoriteManager favoriteManager;

    //возвращает ли исключение если город - фаворит
    @Test
    void addToFavorites_shouldThrowException_whenCityAlreadyFavorite() {
        // given
        City city = City.builder()
                .id(1L)
                .name("Moscow")
                .favoriteStatus(FavoriteStatus.FAVORITE)
                .build();

        // when / then:
        assertThatThrownBy(() -> favoriteManager.addToFavorites(city))
                .isInstanceOf(CityAlreadyFavoriteException.class)
                .hasMessageContaining("Moscow");

        verify(cityRepository, never()).countByFavoriteStatus(any());
        verify(cityRepository, never()).save(any(City.class));
    }

    //возвращает ли исключение если лимит фаворитов превышен
    @Test
    void addToFavorites_shouldThrowException_whenFavoritesLimitExceeded() {
        // given
        City city = City.builder()
                .id(2L)
                .name("Saint Petersburg")
                .favoriteStatus(FavoriteStatus.NOT_FAVORITE)
                .build();

        when(cityRepository.countByFavoriteStatus(FavoriteStatus.FAVORITE)).thenReturn(3L);

        // when / then:
        assertThatThrownBy(() -> favoriteManager.addToFavorites(city))
                .isInstanceOf(FavoritesLimitExceededException.class)
                .hasMessageContaining("Невозможно добавить город " + city.getName() + " в избранное: достигнут лимит (максимум 3 города)");

        verify(cityRepository, times(1)).countByFavoriteStatus(FavoriteStatus.FAVORITE);
        verify(cityRepository, never()).save(any(City.class));
    }

    //добавляется ли город в фавориты если есть свободные места и он не фаворит
    @Test
    void addToFavorites_shouldSetStatusFavoriteAndSave_whenCityNotFavoriteAndLimitNotReached() {
        // given
        City city = City.builder()
                .id(3L)
                .name("London")
                .favoriteStatus(FavoriteStatus.NOT_FAVORITE)
                .build();

        when(cityRepository.countByFavoriteStatus(FavoriteStatus.FAVORITE)).thenReturn(1L);

        // when
        favoriteManager.addToFavorites(city);

        // then
        assertThat(city.getFavoriteStatus()).isEqualTo(FavoriteStatus.FAVORITE);

        verify(cityRepository).countByFavoriteStatus(FavoriteStatus.FAVORITE);
        verify(cityRepository, times(1)).save(city);
    }

    //возвращается ли исключение если город не в фаворитах
    @Test
    void removeFromFavorites_shouldThrowException_whenCityNotFavorite() {
        // given
        City city = City.builder()
                .id(4L)
                .name("Berlin")
                .favoriteStatus(FavoriteStatus.NOT_FAVORITE)
                .build();

        // when / then:
        assertThatThrownBy(() -> favoriteManager.removeFromFavorites(city))
                .isInstanceOf(CityNotFavoriteException.class)
                .hasMessageContaining("Berlin");

        verify(cityRepository, never()).save(any(City.class));
    }

    //меняется ли фаворит на не фаворит
    @Test
    void removeFromFavorites_shouldSetStatusNotFavoriteAndSave_whenCityIsFavorite() {
        // given
        City city = City.builder()
                .id(5L)
                .name("Paris")
                .favoriteStatus(FavoriteStatus.FAVORITE)
                .build();

        // when
        favoriteManager.removeFromFavorites(city);

        // then
        assertThat(city.getFavoriteStatus()).isEqualTo(FavoriteStatus.NOT_FAVORITE);
        verify(cityRepository, times(1)).save(city);
    }

}