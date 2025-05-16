package ru.momo.monitoring.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.momo.monitoring.store.dto.data_generator.RegisterSensorToGeneratorRequest;

import java.util.UUID;

@Component
@Slf4j
public class SensorGeneratorClient {

    private final RestTemplate restTemplate;
    private final String generatorServiceBaseUrl;

    public SensorGeneratorClient(RestTemplate restTemplate,
                                 @Value("${sensor.generator.service.base-url}") String generatorServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.generatorServiceBaseUrl = generatorServiceBaseUrl;
    }

    public void registerSensor(RegisterSensorToGeneratorRequest requestDto) {
        String url = generatorServiceBaseUrl + "/sensors/register";
        log.info("Attempting to register sensor {} with generator service at {}", requestDto.getSensorId(), url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterSensorToGeneratorRequest> requestEntity = new HttpEntity<>(requestDto, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Sensor {} successfully registered with generator service. Response: {}", requestDto.getSensorId(), response.getBody());
            } else {
                log.warn("Sensor {} registration with generator service failed with status {}. Response: {}",
                        requestDto.getSensorId(), response.getStatusCode(), response.getBody());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error registering sensor {} with generator service: {} - {}",
                    requestDto.getSensorId(), e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            log.error("Network error registering sensor {} with generator service: {}", requestDto.getSensorId(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error registering sensor {} with generator service: {}", requestDto.getSensorId(), e.getMessage(), e);
        }
    }

    public void deregisterSensor(UUID sensorId) {
        String url = generatorServiceBaseUrl + "/sensors/" + sensorId.toString() + "/deregister";
        log.info("Attempting to deregister sensor {} with generator service at {}", sensorId, url);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Sensor {} successfully deregistered with generator service. Response: {}", sensorId, response.getBody());
            } else {
                log.warn("Sensor {} deregistration with generator service failed with status {}. Response: {}",
                        sensorId, response.getStatusCode(), response.getBody());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error deregistering sensor {} with generator service: {} - {}",
                    sensorId, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            log.error("Network error deregistering sensor {} with generator service: {}", sensorId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error deregistering sensor {} with generator service: {}", sensorId, e.getMessage(), e);
        }
    }
}