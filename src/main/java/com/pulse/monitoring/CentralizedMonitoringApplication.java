package com.pulse.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CentralizedMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(CentralizedMonitoringApplication.class, args);
    }
}
