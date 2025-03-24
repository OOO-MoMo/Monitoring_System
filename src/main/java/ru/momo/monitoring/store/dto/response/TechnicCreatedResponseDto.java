package ru.momo.monitoring.store.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.entities.User;

import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TechnicCreatedResponseDto {
    Long technicId;
    UUID ownerId;
    String model;
    String brand;

    public static TechnicCreatedResponseDto MapFromEntity(Technic technic, User owner) {
        return TechnicCreatedResponseDto
                .builder()
                .technicId(technic.getTechnicId())
                .ownerId(owner.getId())
                .model(technic.getModel())
                .brand(technic.getBrand())
                .build();
    }
}
