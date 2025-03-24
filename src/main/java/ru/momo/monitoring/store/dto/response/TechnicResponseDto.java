package ru.momo.monitoring.store.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.Technic;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TechnicResponseDto {
    String username;
    String brand;
    String model;
    Long technicId;
    public static TechnicResponseDto mapFromEntity(Technic technic) {
        return TechnicResponseDto
                .builder()
                .technicId(technic.getTechnicId())
                .username(technic.getOwnerId().getEmail())
                .brand(technic.getBrand())
                .model(technic.getModel())
                .build();
    }

}
