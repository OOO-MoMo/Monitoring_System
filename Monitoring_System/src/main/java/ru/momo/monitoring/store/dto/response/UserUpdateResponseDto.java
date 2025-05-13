package ru.momo.monitoring.store.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.User;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateResponseDto {

    String username;

    public static UserUpdateResponseDto mapFromEntity(User user) {
        return UserUpdateResponseDto
                .builder()
                .username(user.getEmail())
                .build();
    }

}
