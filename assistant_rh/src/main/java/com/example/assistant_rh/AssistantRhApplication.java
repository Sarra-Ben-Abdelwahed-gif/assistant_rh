package com.example.assistant_rh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AssistantRhApplication {
    public static void main(String[] args) {
        SpringApplication.run(
            AssistantRhApplication.class, args);
    }
}
