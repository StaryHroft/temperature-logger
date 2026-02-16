package staryhroft.templog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import staryhroft.templog.dto.CityDetailDto;
import staryhroft.templog.dto.CityListViewDto;
import staryhroft.templog.dto.CityRequestDto;
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
    @PostMapping("/city")
    public CityDetailDto getOrCreateCity(@Valid @RequestBody CityRequestDto request){
        return cityService.getOrUpdateCity(request.getName());
    }

    //Получить количество городов
    @GetMapping("/count")
    public long getTotalCityCount(){
        return cityService.getCityCount();
    }

    //Удалить город по названию
    @DeleteMapping("/city")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCity(@Valid @RequestBody CityRequestDto request){
        cityService.deleteCity(request.getName());
    }

    //Удалить все города
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAllCities(){
        cityService.deleteAllCities();
    }

    //Добавить город в избранное
    @PostMapping("/favorite/add")
    @ResponseStatus(HttpStatus.OK)
    public void addCityToFavorites(@Valid @RequestBody CityRequestDto request){
        cityService.markAsFavorite(request.getName());
    }

    //Удалить город из избранного
    @PostMapping("/favorite/remove")
    @ResponseStatus(HttpStatus.OK)
    public void removeCityFromFavorites(@Valid @RequestBody CityRequestDto request){
        cityService.unmarkFromFavorite(request.getName());
    }
}
