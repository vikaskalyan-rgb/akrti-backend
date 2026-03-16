package com.akriti.apartment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApartmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApartmentApplication.class, args);
    }
}
