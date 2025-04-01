package ru.momo.monitoring.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.User;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Ответ с данными пользователя")
public class UserResponseDto {

    @Schema(description = "Уникальный идентификатор пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id;

    @Schema(description = "Email пользователя", example = "user@example.com")
    String email;

    @Schema(description = "Имя пользователя", example = "Иван")
    String firstname;

    @Schema(description = "Фамилия пользователя", example = "Иванов")
    String lastname;

    @Schema(description = "Отчество пользователя", example = "Иванович")
    String patronymic;

    @Schema(description = "Номер телефона", example = "+79001234567")
    String phoneNumber;

    @Schema(description = "Дата рождения", example = "1990-05-20")
    LocalDate dateOfBirth;

    @Schema(description = "Адрес пользователя", example = "Москва, ул. Ленина, д. 10")
    String address;

    @Schema(description = "Организация", example = "ООО 'Компания'")
    String organization;

    public static UserResponseDto mapFromEntity(User user) {
        return UserResponseDto
                .builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstname(user.getUserData().getFirstname())
                .lastname(user.getUserData().getLastname())
                .patronymic(user.getUserData().getPatronymic())
                .phoneNumber(user.getUserData().getPhoneNumber())
                .dateOfBirth(user.getUserData().getDateOfBirth())
                .address(user.getUserData().getAddress())
                .organization(user.getUserData().getOrganization())
                .build();
    }

}
