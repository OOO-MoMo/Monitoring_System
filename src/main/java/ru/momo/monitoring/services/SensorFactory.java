package ru.momo.monitoring.services;

import ru.momo.monitoring.store.entities.sensor.AbstractSensor;

public interface SensorFactory {

    AbstractSensor getSensor(String sensorType);

}
