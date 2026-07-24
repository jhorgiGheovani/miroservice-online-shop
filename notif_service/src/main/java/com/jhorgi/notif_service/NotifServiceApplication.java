package com.jhorgi.notif_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class NotifServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifServiceApplication.class, args);
    }

}
