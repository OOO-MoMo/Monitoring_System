package ru.momo.monitoring.store.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private SensorType type;

    @Column(name = "serial_number", unique = true, nullable = false)
    private String serialNumber;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "min_value")
    private String minValue;

    @Column(name = "max_value")
    private String maxValue;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "calibration_due_date")
    private LocalDate calibrationDueDate;
}
