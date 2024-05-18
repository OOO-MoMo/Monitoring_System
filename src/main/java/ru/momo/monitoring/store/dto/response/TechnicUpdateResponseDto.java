package ru.momo.monitoring.store.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.Technic;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TechnicUpdateResponseDto {
    String brand;
    String model;

    public static TechnicUpdateResponseDto mapFromEntity(Technic technic) {
        return TechnicUpdateResponseDto
                .builder()
                .brand(technic.getBrand())
                .model(technic.getModel())
                .build();
    }
}
