package ru.momo.monitoring.store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.Sensor;
import ru.momo.monitoring.store.entities.Technic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TechnicResponseDto {

    String username;

    @JsonProperty(value = "technic_id")
    UUID technicId;

    String brand;

    String model;

    Integer year;

    @JsonProperty(value = "serial_number")
    String serialNumber;

    String vin;

    String description;

    Boolean active;

    @JsonProperty(value = "last_service_date")
    LocalDateTime lastServiceDate;

    @JsonProperty(value = "next_service_date")
    LocalDateTime nextServiceDate;

    CompanyResponseDto company;

    List<UUID> sensors;

    public static TechnicResponseDto mapFromEntity(Technic technic) {
        return TechnicResponseDto
                .builder()
                .technicId(technic.getId())
                .username(technic.getOwnerId() != null ? technic.getOwnerId().getEmail() : "Водитель не назначен")
                .brand(technic.getBrand())
                .model(technic.getModel())
                .year(technic.getYear())
                .serialNumber(technic.getSerialNumber())
                .vin(technic.getVin())
                .description(technic.getDescription())
                .active(technic.getIsActive())
                .lastServiceDate(technic.getLastServiceDate())
                .nextServiceDate(technic.getNextServiceDate())
                .company(CompanyResponseDto.mapFromEntity(technic.getCompany()))
                .sensors(technic.getSensors().stream().map(Sensor::getId).collect(Collectors.toList()))
                .build();
    }

}
