package com.example.animalvoiceprint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AnimalVoiceprintApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnimalVoiceprintApplication.class, args);
    }
}