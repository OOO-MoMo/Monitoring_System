package ru.momo.monitoring.store.entities.sensor;

public class SpeedometerSensor extends AbstractSensor {

    private final double MIN_VALUE = 1.0;

    private final double MAX_VALUE = 45.0;

    @Override
    public String calculateData() {
        return "Current speed is: " +
                decimalFormat.format(
                        MIN_VALUE + (MAX_VALUE - MIN_VALUE) * random.nextDouble()
                )
                + " km/h";
    }

}
