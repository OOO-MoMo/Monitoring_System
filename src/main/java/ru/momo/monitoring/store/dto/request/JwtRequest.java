package ru.momo.monitoring.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtRequest {

    @NotNull(message = "Username must be not null")
    String username;

    @NotNull(message = "Password must be not null")
    String password;

}
