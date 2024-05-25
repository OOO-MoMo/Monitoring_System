package ru.momo.monitoring.store.entities.sensor;

public class FuelSensor extends AbstractSensor{

    private final double MIN_VALUE = 0.0;

    private final double MAX_VALUE = 100.0;

    @Override
    public String calculateData() {
        return "Current fuel balance : " +
                decimalFormat.format(
                        MIN_VALUE + (MAX_VALUE - MIN_VALUE) * random.nextDouble()
                )
                + " %";
    }

}
