package ru.momo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SensorDataGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SensorDataGeneratorApplication.class, args);
    }

}