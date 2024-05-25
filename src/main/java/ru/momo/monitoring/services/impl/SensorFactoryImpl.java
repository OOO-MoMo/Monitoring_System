package ru.momo.monitoring.services.impl;

import org.springframework.stereotype.Service;
import ru.momo.monitoring.exceptions.SensorNotCreatedException;
import ru.momo.monitoring.services.SensorFactory;
import ru.momo.monitoring.store.entities.sensor.AbstractSensor;
import ru.momo.monitoring.store.entities.sensor.FuelSensor;
import ru.momo.monitoring.store.entities.sensor.PressureSensor;
import ru.momo.monitoring.store.entities.sensor.SpeedometerSensor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class SensorFactoryImpl implements SensorFactory {

    private final Map<String, Supplier<AbstractSensor>> sensorRegistry;

    {
        sensorRegistry = new HashMap<>();
    }

    public SensorFactoryImpl() {
        registerSensor("speedometer", SpeedometerSensor::new);
        registerSensor("fuel_sensor", FuelSensor::new);
        registerSensor("pressure_sensor", PressureSensor::new);
    }

    @Override
    public void registerSensor(String sensorType, Supplier<AbstractSensor> sensorSupplier) {
        sensorRegistry.put(sensorType, sensorSupplier);
    }

    @Override
    public AbstractSensor getSensor(String sensorType) {
        Supplier<AbstractSensor> sensorSupplier = sensorRegistry.get(sensorType);

        if (sensorSupplier != null) {
            return sensorSupplier.get();
        } else {
            throw new SensorNotCreatedException(
                    "Sensor : %s is not defined in server", sensorType
            );
        }
    }

}
