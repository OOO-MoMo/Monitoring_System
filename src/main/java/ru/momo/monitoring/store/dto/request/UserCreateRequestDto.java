package ru.momo.monitoring.store.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequestDto {

    @NotNull(message = "Username must be not null")
    @Length(max = 255, message = "Username length must be smaller than 255 symbols")
    String username;

    @NotNull(message = "Password must be not null")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password;

    @NotNull(message = "Confirmation must be not null")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String passwordConfirmation;

    @NotNull(message = "Firstname must be not null")
    @Length(max = 255, message = "Firstname length must be smaller than 255 symbols")
    String firstname;

    @NotNull(message = "Lastname must be not null")
    @Length(max = 255, message = "Lastname length must be smaller than 255 symbols")
    String lastname;

    @Length(max = 255, message = "Patronymic length must be smaller than 255 symbols")
    String patronymic;

    @Pattern(regexp = "^[78]\\d{10}$", message = "Phone number must be 11 digits and start with 7 or 8")
    String phoneNumber;

    public static User mapToUserEntity(UserCreateRequestDto user) {
        return User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }

    public static UserData mapToUserDataEntity(UserCreateRequestDto user) {
        return UserData
                .builder()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .patronymic(user.getPatronymic())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

}
