package staryhroft.templog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import staryhroft.templog.dto.ApiResponse;
import staryhroft.templog.dto.CityDetailDto;
import staryhroft.templog.dto.CityRequestDto;
import staryhroft.templog.service.CityService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@Slf4j
public class CityController {

    private final CityService cityService;

    //Показать список городов
    @GetMapping
    public ApiResponse<List<CityDetailDto>> fetchAllCities(){
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
    public void removeCity(@Valid @RequestBody CityRequestDto request){
        cityService.deleteCity(request.getName());
    }

    //Удалить все города
    @DeleteMapping
    public void removeAllCities(){
        cityService.deleteAllCities();
    }

    //Добавить город в избранное
    @PostMapping("/favorite/add")
    public ApiResponse<Void> addCityToFavorites(@Valid @RequestBody CityRequestDto request){
        cityService.markAsFavorite(request.getName());
        return new ApiResponse<>(LocalDateTime.now(),
                "Город " + request.getName() + " добавлен в избранное", null);
    }

    //Удалить город из избранного
    @PostMapping("/favorite/remove")
    public ApiResponse<Void> removeCityFromFavorites(@Valid @RequestBody CityRequestDto request){
        cityService.unmarkFromFavorite(request.getName());
        return new ApiResponse<>(LocalDateTime.now(),
                "Город " + request.getName() + " удалён из избранного", null);

    }
}
