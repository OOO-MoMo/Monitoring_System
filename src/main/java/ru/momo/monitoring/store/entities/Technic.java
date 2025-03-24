package ru.momo.monitoring.store.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "technics")
public class Technic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "technic_id")
    private Long technicId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    User ownerId;

    @Column(name = "model")
    private String model;

    @Column(name = "brand")
    private String brand;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "technics_sensors",
            joinColumns = @JoinColumn(name = "technic_id"),
            inverseJoinColumns = @JoinColumn(name = "sensor_id")
    )
    private Set<Sensor> sensors;

}
