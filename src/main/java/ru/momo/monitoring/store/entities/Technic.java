package ru.momo.monitoring.store.entities;

import jakarta.persistence.*;
import lombok.*;

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
    @JoinTable(joinColumns = @JoinColumn(name = "technic_id"))
    Set<Sensor> sensors;

}
