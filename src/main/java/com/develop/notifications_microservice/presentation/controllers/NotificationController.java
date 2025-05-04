package com.develop.notifications_microservice.presentation.controllers;

import com.develop.notifications_microservice.domain.interfaces.NotificationServicePort;
import com.develop.notifications_microservice.domain.models.Notification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationServicePort service;

    public NotificationController(NotificationServicePort service) {
        this.service = service;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(@PathVariable Long id) {
        try {
            service.updateNotificationStatus(id, true);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Notification updated successfully."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Notification not found with ID: " + id));
        }
    }

    @GetMapping("/{userId}")
    public List<Notification> getByUser(@PathVariable Long userId) {
        return service.getNotificationsByUserId(userId);
    }
}
