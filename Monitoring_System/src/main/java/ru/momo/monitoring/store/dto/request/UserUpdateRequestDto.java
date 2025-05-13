package ru.momo.monitoring.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Запрос на обновление данных пользователя")
public class UserUpdateRequestDto {

    @Size(max = 255, message = "Имя не должно превышать 255 символов")
    @Schema(description = "Имя пользователя", example = "Иван")
    String firstname;

    @Size(max = 255, message = "Фамилия не должна превышать 255 символов")
    @Schema(description = "Фамилия пользователя", example = "Иванов")
    String lastname;

    @Size(max = 255, message = "Отчество не должно превышать 255 символов")
    @Schema(description = "Отчество пользователя", example = "Иванович")
    String patronymic;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Некорректный номер телефона")
    @Schema(description = "Номер телефона", example = "+79001234567")
    String phoneNumber;

    @Past(message = "Дата рождения должна быть в прошлом")
    @Schema(description = "Дата рождения", example = "1990-05-20")
    LocalDate dateOfBirth;

    @Size(max = 255, message = "Адрес не должен превышать 255 символов")
    @Schema(description = "Адрес пользователя", example = "Москва, ул. Ленина, д. 10")
    String address;

}
