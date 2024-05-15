package ru.momo.monitoring.store.dto.response;

import lombok.*;
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
                //.username(user.getUsername())
                .brand(technic.getBrand())
                .model(technic.getModel())
                .build();
    }


}
