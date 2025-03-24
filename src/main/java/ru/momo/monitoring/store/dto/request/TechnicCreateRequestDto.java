package ru.momo.monitoring.store.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.entities.User;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TechnicCreateRequestDto {
    @NotNull(message = "Owner Id must be not null")
    @JsonProperty("ownerId")
    UUID ownerId;

    @NotNull(message = "Model must be not null")
    @Length(max = 255, message = "Model length must be smaller than 255 symbols")
    String model;

    @NotNull(message = "Brand must be not null")
    @Length(max = 255, message = "Brand length must be smaller than 255 symbols")
    String brand;

    public static Technic mapToTechnicEntity(TechnicCreateRequestDto technic, User owner) {
        return Technic
                .builder()
                .ownerId(owner)
                .model(technic.getModel())
                .brand(technic.getBrand())
                .build();
    }

}
