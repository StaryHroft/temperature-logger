package staryhroft.templog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.CityTemperature;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityTemperatureRepository extends JpaRepository<CityTemperature, Long> {

    Optional<CityTemperature> findFirstByCityOrderByTimestampDesc(City city);
}
