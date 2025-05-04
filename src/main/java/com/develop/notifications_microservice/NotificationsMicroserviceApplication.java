package com.develop.notifications_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotificationsMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationsMicroserviceApplication.class, args);
	}

}
