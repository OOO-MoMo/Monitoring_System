package ru.momo.monitoring.store.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "sensors")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_id")
    private Long sensorId;

    @Column(name = "type")
    String type;

    @Column(name = "data_type")
    String dataType;

    @ManyToMany(mappedBy = "sensors")
    private Set<Technic> technics;

}
