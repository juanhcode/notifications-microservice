package com.develop.notifications_microservice.infrastructure.repositories;

import com.develop.notifications_microservice.domain.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaNotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
}
