package ru.momo.monitoring.services;

import org.springframework.stereotype.Component;
import ru.momo.monitoring.store.entities.sensor.AbstractSensor;

import java.util.function.Supplier;

@Component
public interface SensorFactory {

    AbstractSensor getSensor(String sensorType);

    void registerSensor(String sensorType, Supplier<AbstractSensor> sensorSupplier);

}
