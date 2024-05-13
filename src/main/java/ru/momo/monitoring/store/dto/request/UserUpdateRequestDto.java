package ru.momo.monitoring.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequestDto {

    @NotNull(message = "Username must be not null")
    @Length(max = 255, message = "Username length must be smaller than 255 symbols")
    String oldUsername;

    @NotNull(message = "Username must be not null")
    @Length(max = 255, message = "Username length must be smaller than 255 symbols")
    String newUsername;

}
