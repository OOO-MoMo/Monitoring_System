package ru.momo.monitoring.store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO с информацией о единице техники для ответа API")
public class TechnicResponseDto {

    @JsonProperty(value = "driver_id")
    @Schema(description = "Id пользователя (водителя), закрепленного за техникой, или 'Водитель не назначен'(UUID)",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    String userId;

    @JsonProperty(value = "technic_id")
    @Schema(description = "Уникальный идентификатор техники (UUID)",
            name = "technic_id",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    UUID technicId;

    @Schema(description = "Бренд техники", example = "JCB")
    String brand;

    @Schema(description = "Модель техники", example = "3CX")
    String model;

    @Schema(description = "Год выпуска техники", example = "2020")
    Integer year;

    @JsonProperty(value = "serial_number")
    @Schema(description = "Серийный номер техники",
            name = "serial_number",
            example = "SN-JCB3CX-12345")
    String serialNumber;

    @Schema(description = "VIN (идентификационный номер транспортного средства)",
            example = "VIN123456789ABCDEFG")
    String vin;

    @Schema(description = "Описание техники", example = "Экскаватор-погрузчик в рабочем состоянии")
    String description;

    @Schema(description = "Статус активности техники (true - активна, false - неактивна)", example = "true")
    Boolean active;

    @JsonProperty(value = "last_service_date")
    @Schema(description = "Дата и время последнего обслуживания",
            name = "last_service_date",
            example = "2023-10-26T14:30:00", format = "date-time")
    LocalDateTime lastServiceDate;

    @JsonProperty(value = "next_service_date")
    @Schema(description = "Планируемая дата и время следующего обслуживания",
            name = "next_service_date",
            example = "2024-04-26T10:00:00", format = "date-time")
    LocalDateTime nextServiceDate;

    @Schema(description = "Информация о компании-владельце техники")
    CompanyResponseDto company; // Swagger автоматически подтянет схему для CompanyResponseDto

    @Schema(description = "Список идентификаторов (UUID) сенсоров, установленных на данной технике")
    @ArraySchema(schema = @Schema(description = "UUID сенсора", example = "b1b2c3d4-e5f6-7890-1234-567890abcdef", type = "string", format = "uuid"))
    List<UUID> sensors;

    /**
     * Статический метод для преобразования сущности Technic в TechnicResponseDto.
     *
     * @param technic Сущность техники.
     * @return DTO с данными о технике.
     */
    public static TechnicResponseDto mapFromEntity(Technic technic) {
        String ownerUsername = (technic.getOwnerId() != null && technic.getOwnerId().getEmail() != null)
                ? technic.getOwnerId().getId().toString()
                : "Водитель не назначен";

        CompanyResponseDto companyDto = (technic.getCompany() != null)
                ? CompanyResponseDto.mapFromEntity(technic.getCompany())
                : null;

        List<UUID> sensorIds = (technic.getSensors() != null)
                ? technic.getSensors().stream().map(Sensor::getId).collect(Collectors.toList())
                : List.of();

        return TechnicResponseDto
                .builder()
                .technicId(technic.getId())
                .userId(ownerUsername)
                .brand(technic.getBrand())
                .model(technic.getModel())
                .year(technic.getYear())
                .serialNumber(technic.getSerialNumber())
                .vin(technic.getVin())
                .description(technic.getDescription())
                .active(technic.getIsActive())
                .lastServiceDate(technic.getLastServiceDate())
                .nextServiceDate(technic.getNextServiceDate())
                .company(companyDto)
                .sensors(sensorIds)
                .build();
    }
}
