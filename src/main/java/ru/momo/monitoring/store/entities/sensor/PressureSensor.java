package ru.momo.monitoring.store.entities.sensor;

public class PressureSensor extends AbstractSensor {

    private final double MIN_VALUE = 10.0;

    private final double MAX_VALUE = 50.0;

    @Override
    public String calculateData() {
        return "Current pressure is: " +
                (MIN_VALUE + (MAX_VALUE - MIN_VALUE) * random.nextDouble())
                + " PMa";
    }

}
