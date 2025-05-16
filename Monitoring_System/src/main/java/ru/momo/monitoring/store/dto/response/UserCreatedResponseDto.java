package ru.momo.monitoring.store.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreatedResponseDto {

    UUID userId;
    String username;

    String firstname;

    String lastname;

    String patronymic;

    String phoneNumber;

    public static UserCreatedResponseDto mapFromEntity(User user, UserData data) {
        return UserCreatedResponseDto
                .builder()
                .userId(user.getId())
                .username(user.getEmail())
                .firstname(data.getFirstname())
                .lastname(data.getLastname())
                .patronymic(data.getPatronymic())
                .phoneNumber(data.getPhoneNumber())
                .build();
    }
}
