package ru.momo.monitoring.store.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreatedResponseDto {

    Long userId;
    String username;

    String firstname;

    String lastname;

    String patronymic;

    String phoneNumber;

    public static UserCreatedResponseDto MapFromEntity(User user, UserData data) {
        return UserCreatedResponseDto
                .builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .firstname(data.getFirstname())
                .lastname(data.getLastname())
                .patronymic(data.getPatronymic())
                .phoneNumber(data.getPhoneNumber())
                .build();
    }
}
