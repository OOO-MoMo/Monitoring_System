package ru.momo.monitoring.services.impl;

import ru.momo.monitoring.exceptions.SensorNotCreatedException;
import ru.momo.monitoring.services.SensorFactory;
import ru.momo.monitoring.store.entities.sensor.AbstractSensor;
import ru.momo.monitoring.store.entities.sensor.FuelSensor;
import ru.momo.monitoring.store.entities.sensor.PressureSensor;
import ru.momo.monitoring.store.entities.sensor.SpeedometerSensor;

public class SensorFactoryImpl implements SensorFactory {

    @Override
    public AbstractSensor getSensor(String sensorType) {
        AbstractSensor sensor;

        switch (sensorType) {
            case "speedometer" -> sensor = new SpeedometerSensor();
            case "fuel_sensor" -> sensor = new FuelSensor();
            case "pressure_sensor" -> sensor = new PressureSensor();
            default -> throw new SensorNotCreatedException(
                    "Sensor : %s is not defined in server", sensorType
            );
        }

        return sensor;
    }

}
