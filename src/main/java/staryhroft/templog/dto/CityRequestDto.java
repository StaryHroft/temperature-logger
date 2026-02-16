package staryhroft.templog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CityRequestDto {
    @NotBlank(message = "Название города не может быть пустым")
    @Pattern(regexp = "^[A-Za-z]+(?:[ -][A-Za-z]+)?$",
            message = "Название города должно содержать только латинские буквы, " +
                    "может состоять из одного или двух слов, разделенных пробелом или дефисом")
    private String name;
}
