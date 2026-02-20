package staryhroft.templog.controller;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import staryhroft.templog.advice.GlobalExceptionHandler;
import staryhroft.templog.dto.ApiResponse;
import staryhroft.templog.dto.CityDetailDto;
import staryhroft.templog.dto.CityRequestDto;
import staryhroft.templog.entity.enums.FavoriteStatus;
import staryhroft.templog.exception.business.CityAlreadyFavoriteException;
import staryhroft.templog.exception.business.CityNotFavoriteException;
import staryhroft.templog.exception.business.CityNotFoundException;
import staryhroft.templog.exception.business.FavoritesLimitExceededException;
import staryhroft.templog.service.CityService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CityController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CityService cityService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final String validCityName = "Moscow";

    private final CityRequestDto validRequest = createCityRequest(validCityName);

    private CityRequestDto createCityRequest(String name) {
        CityRequestDto dto = new CityRequestDto();
        dto.setName(name);
        return dto;
    }

    //ля возврата всех городов
    @Test
    void fetchAllCities_shouldReturnListOfCities() throws Exception {
        //given
        List<CityDetailDto> cities = Arrays.asList(
                new CityDetailDto("Moscow", FavoriteStatus.FAVORITE, 10.0, LocalDateTime.now()),
                new CityDetailDto("London", FavoriteStatus.NOT_FAVORITE, null, null)
        );
        ApiResponse<List<CityDetailDto>> expectedResponse = new ApiResponse<>(LocalDateTime.now(), null, cities);
        when(cityService.getAllCities()).thenReturn(expectedResponse);

        //when
        mockMvc.perform(get("/api/cities"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.data[0].name").value("Moscow"))
                .andExpect(jsonPath("$.data[0].favoriteStatus").value("FAVORITE"))
                .andExpect(jsonPath("$.data[0].lastTemperature").value(10.0))
                .andExpect(jsonPath("$.data[0].lastTemperatureTime").isNotEmpty())
                .andExpect(jsonPath("$.data[1].name").value("London"))
                .andExpect(jsonPath("$.data[1].favoriteStatus").value("NOT_FAVORITE"))
                .andExpect(jsonPath("$.data[1].lastTemperature").doesNotExist());
    }

    //для возврата пустого списка
    @Test
    void fetchAllCities_shouldReturnEmptyListWithMessage() throws Exception {
        //given
        ApiResponse<List<CityDetailDto>> emptyResponse =
                new ApiResponse<>(LocalDateTime.now(), "Список городов пуст", Collections.emptyList());
        when(cityService.getAllCities()).thenReturn(emptyResponse);

        //when / then
        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Список городов пуст"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    //ри правильном запросе - правильный ответ
    @Test
    void getOrCreateCity_shouldReturnCityDetailDto() throws Exception {
        //given
        CityDetailDto dto = new CityDetailDto(validCityName, FavoriteStatus.FAVORITE,
                10.0, LocalDateTime.now());
        when(cityService.getOrUpdateCity(validCityName)).thenReturn(dto);

        //when
        mockMvc.perform(post("/api/cities/city")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))

                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(validCityName))
                .andExpect(jsonPath("$.favoriteStatus").value("FAVORITE"))
                .andExpect(jsonPath("$.lastTemperature").value(10.0))
                .andExpect(jsonPath("$.lastTemperatureTime").isNotEmpty());
    }

    //при отправке имени из пробелов - ошибка
    @Test
    void getOrCreateCity_shouldReturnBadRequest_whenCityNameIsBlank() throws Exception {
        //given
        CityRequestDto invalidRequest = createCityRequest("   ");

        //when / then
        mockMvc.perform(post("/api/cities/city")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.title").value("Ошибка запроса"))
                .andExpect(jsonPath("$.message").exists());
    }

    //при CityNotFoundException в сервисе - 404 и сообщение
    @Test
    void getOrCreateCity_shouldReturnNotFound_whenCityNotFound() throws Exception {
        //given
        when(cityService.getOrUpdateCity(validCityName))
                .thenThrow(new CityNotFoundException(validCityName));

        //when / then
        mockMvc.perform(post("/api/cities/city")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Город Moscow отсутствует в базе данных"));
    }

    //при правильном запросе - статус 200ок
    @Test
    void removeCity_shouldReturnOk_whenCityDeleted() throws Exception {
        //given
        doNothing().when(cityService).deleteCity(validCityName);

        //when
        mockMvc.perform(delete("/api/cities/city")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                // then
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    //при неправильном запросе - исключение
    @Test
    void removeCity_shouldReturnBadRequest_whenCityNameIsBlank() throws Exception {
        // given
        CityRequestDto invalidRequest = createCityRequest("   ");

        // when & then
        mockMvc.perform(delete("/api/cities/city")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.title").value("Ошибка запроса")) // или "Validation Failed", смотря что в вашем обработчике
                .andExpect(jsonPath("$.message").exists());
    }

    //при CityNotFoundException в сервисе - статус и сообщенгие
    @Test
    void removeCity_shouldReturnNotFound_whenCityDoesNotExist() throws Exception {
        // given
        doThrow(new CityNotFoundException(validCityName))
                .when(cityService).deleteCity(validCityName);

        // when & then
        mockMvc.perform(delete("/api/cities/city")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Город Moscow отсутствует в базе данных"));
    }

    //успешный сценарий
    @Test
    void addCityToFavorites_shouldReturnSuccessMessage() throws Exception {
        // given
        doNothing().when(cityService).markAsFavorite(validCityName);

        // when
        mockMvc.perform(post("/api/cities/favorite/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Город Moscow добавлен в избранное"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    //если имя пустое
    @Test
    void addCityToFavorites_shouldReturnBadRequest_whenCityNameIsBlank() throws Exception {
        // given
        CityRequestDto invalidRequest = createCityRequest("   ");

        // when & then
        mockMvc.perform(post("/api/cities/favorite/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.title").value("Ошибка запроса"))
                .andExpect(jsonPath("$.message").exists());
    }

    //при CityNotFoundException в сервисе
    @Test
    void addCityToFavorites_shouldReturnNotFound_whenCityDoesNotExist() throws Exception {
        // given
        doThrow(new CityNotFoundException(validCityName))
                .when(cityService).markAsFavorite(validCityName);

        // when & then
        mockMvc.perform(post("/api/cities/favorite/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Город Moscow отсутствует в базе данных"));
    }

    //при CityAlreadyFavoriteException в сервисе
    @Test
    void addCityToFavorites_shouldReturnBadRequest_whenAlreadyFavorite() throws Exception {
        // given
        doThrow(new CityAlreadyFavoriteException(validCityName))
                .when(cityService).markAsFavorite(validCityName);

        // when & then
        mockMvc.perform(post("/api/cities/favorite/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Город Moscow уже находится в избранном"));
    }

    //при FavoritesLimitExceededException в сервисе
    @Test
    void addCityToFavorites_shouldReturnBadRequest_whenFavoritesLimitExceeded() throws Exception {
        // given
        doThrow(new FavoritesLimitExceededException("Достигнут лимит избранных городов (3)"))
                .when(cityService).markAsFavorite(validCityName);

        // when & then
        mockMvc.perform(post("/api/cities/favorite/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Невозможно добавить город " +
                        "Достигнут лимит избранных городов (3) в избранное: " +
                        "достигнут лимит (максимум 3 города)."));
    }

    //правильная работа
    @Test
    void removeCityFromFavorites_shouldReturnSuccessMessage() throws Exception {
        // given
        doNothing().when(cityService).unmarkFromFavorite(validCityName);

        // when
        mockMvc.perform(post("/api/cities/favorite/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Город Moscow удалён из избранного"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    //при неправильном имени - ошибка
    @Test
    void removeCityFromFavorites_shouldReturnBadRequest_whenCityNameIsBlank() throws Exception {
        // given
        CityRequestDto invalidRequest = createCityRequest("   ");

        // when & then
        mockMvc.perform(post("/api/cities/favorite/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())                          // 400
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.title").value("Ошибка запроса"))
                .andExpect(jsonPath("$.message").exists());
    }

    //если у сервиса CityNotFoundException - ошибка
    @Test
    void removeCityFromFavorites_shouldReturnNotFound_whenCityDoesNotExist() throws Exception {
        // given
        doThrow(new CityNotFoundException(validCityName))
                .when(cityService).unmarkFromFavorite(validCityName);

        // when & then
        mockMvc.perform(post("/api/cities/favorite/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())                             // 404
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Город Moscow отсутствует в базе данных"));
    }

    //если город не в избранном - ошибка
    @Test
    void removeCityFromFavorites_shouldReturnBadRequest_whenNotFavorite() throws Exception {
        // given
        doThrow(new CityNotFavoriteException(validCityName))
                .when(cityService).unmarkFromFavorite(validCityName);

        // when & then
        mockMvc.perform(post("/api/cities/favorite/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Город Moscow отсутствует в списке избранных городов"));
    }
}