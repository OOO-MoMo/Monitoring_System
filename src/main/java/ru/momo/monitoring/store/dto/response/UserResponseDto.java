package ru.momo.monitoring.store.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponseDto {

    String username;
    String firstname;
    String lastname;
    String patronymic;
    String phoneNumber;

    public static UserResponseDto mapFromEntity(User user, UserData data) {
        return UserResponseDto
                .builder()
                .username(user.getEmail())
                .firstname(data.getFirstname())
                .lastname(data.getLastname())
                .patronymic(data.getPatronymic())
                .phoneNumber(data.getPhoneNumber())
                .build();
    }

}
