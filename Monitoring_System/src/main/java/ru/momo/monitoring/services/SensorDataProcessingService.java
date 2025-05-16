package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.data_generator.GeneratedSensorDataDto;

public interface SensorDataProcessingService {

    void processIncomingData(GeneratedSensorDataDto incomingData);

}