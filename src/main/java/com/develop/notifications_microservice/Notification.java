package com.develop.notifications_microservice;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class Notification {

    @GetMapping
    public String getNotification() {
        return "Hello, this is a notification!!";
    }
}
