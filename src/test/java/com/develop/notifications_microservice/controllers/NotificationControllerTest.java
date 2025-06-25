package com.develop.notifications_microservice.controllers;

import com.develop.notifications_microservice.domain.interfaces.NotificationServicePort;
import com.develop.notifications_microservice.domain.models.Notification;
import com.develop.notifications_microservice.presentation.controllers.NotificationController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    NotificationServicePort service;

    @InjectMocks
    NotificationController controller;

    @Test
    void update_shouldReturnOk_whenNotificationExists() {
        Long id = 1L;
        doNothing().when(service).updateNotificationStatus(id, true);

        ResponseEntity<Map<String, String>> response = controller.update(id);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Notification updated successfully.", response.getBody().get("message"));
        verify(service, times(1)).updateNotificationStatus(id, true);
    }

    @Test
    void update_shouldReturnNotFound_whenNotificationDoesNotExist() {
        Long id = 1L;
        doThrow(new IllegalArgumentException("Notification not found with ID: " + id))
                .when(service).updateNotificationStatus(id, true);

        ResponseEntity<Map<String, String>> response = controller.update(id);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Notification not found with ID: " + id, response.getBody().get("error"));
        verify(service, times(1)).updateNotificationStatus(id, true);
    }

    @Test
    void getByUser_shouldReturnNotificationsList() {
        Long userId = 5L;
        List<Notification> mockList = List.of(
                new Notification(1L, userId, "desc1", 10L, true, LocalDateTime.now(), "Título 1"),
                new Notification(2L, userId, "desc2", 11L, true, LocalDateTime.now(), "Título 2")
        );
        when(service.getNotificationsByUserId(userId)).thenReturn(mockList);

        List<Notification> response = controller.getByUser(userId);

        assertNotNull(response);
        assertEquals(2, response.size());
        verify(service, times(1)).getNotificationsByUserId(userId);
    }
}
