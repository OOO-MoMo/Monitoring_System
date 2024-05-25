package ru.momo.monitoring.store.entities.sensor;

import java.util.Random;

abstract public class AbstractSensor {

    protected final Random random;

    protected AbstractSensor() {
        this.random = new Random();
    }

    abstract public String calculateData();

}
