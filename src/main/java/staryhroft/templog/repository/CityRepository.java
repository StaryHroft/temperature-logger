package staryhroft.templog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.enums.FavoriteStatus;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    //Поиск города по имени
    Optional<City> findByName(String name);

    //Подсчет количества избранных городов
    long countByFavoriteStatus(FavoriteStatus status);

    //получение отсортированного списка городов
    List<City> findByFavoriteStatusOrderByIdDesc(FavoriteStatus status);
}
