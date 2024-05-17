package ru.momo.monitoring.store.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class JwtResponse {

    Long id;

    String username;

    String accessToken;

    String refreshToken;

}
