package ru.momo.monitoring.store.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.entities.User;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TechnicCreatedResponseDto {
    Long technicId;
    Long ownerId;
    String model;
    String brand;

    public static TechnicCreatedResponseDto MapFromEntity(Technic technic, User owner) {
        return TechnicCreatedResponseDto
                .builder()
                .technicId(technic.getTechnicId())
                .ownerId(owner.getUserId())
                .model(technic.getModel())
                .brand(technic.getBrand())
                .build();
    }
}
