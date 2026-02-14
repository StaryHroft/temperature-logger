package staryhroft.templog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import staryhroft.templog.dto.CityDetailDto;
import staryhroft.templog.dto.CityListViewDto;
import staryhroft.templog.service.CityService;
import staryhroft.templog.service.WeatherService;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@Slf4j
public class CityController {

    private final CityService cityService;

    //Показать список городов
    @GetMapping
    public List<CityListViewDto> fetchAllCities(){
        return cityService.getAllCities();
    }

    //Показать город по названию
    @GetMapping("/{cityName}")
    public CityDetailDto getCity(@PathVariable String cityName){
        return cityService.getOrUpdateCity(cityName);
    }

    //Получить количество городов
    @GetMapping("/count")
    public long getTotalCityCount(){
        return cityService.getCityCount();
    }

    //Удалить город по названию
    @DeleteMapping("/{cityName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCity(@PathVariable String cityName){
        cityService.deleteCity(cityName);
    }

    //Удалить все города
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAllCities(){
        cityService.deleteAllCities();
    }

    //Добавить город в избранное
    @PostMapping("/{cityName}/favorite")
    @ResponseStatus(HttpStatus.OK)
    public void addCityToFavorites(@PathVariable String cityName){
        cityService.markAsFavorite(cityName);
    }

    //Удалить город из избранного
    @DeleteMapping("/{cityName}/favorite")
    @ResponseStatus(HttpStatus.OK)
    public void removeCityFromFavorites(@PathVariable String cityName){
        cityService.unmarkFromFavorite(cityName);
    }
}
