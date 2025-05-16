package ru.momo.monitoring.store.entities.sensor;

import java.text.DecimalFormat;
import java.util.Random;

abstract public class AbstractSensor {

    protected final Random random;

    protected final DecimalFormat decimalFormat;

    protected AbstractSensor() {
        this.random = new Random();
        this.decimalFormat = new DecimalFormat("#.##");
    }

    abstract public String calculateData();

}
