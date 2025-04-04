package ru.momo.monitoring.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TechnicUpdateRequestDto {
    @NotNull(message = "Technic Id must be not null")
    UUID technicId;

    @NotNull(message = "Brand must be not null")
    @Length(max = 255, message = "Brand length must be smaller than 255 symbols")
    String newBrand;

    @Length(max = 255, message = "Model length must be smaller than 255 symbols")
    String newModel;

}
