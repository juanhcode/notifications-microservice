package com.develop.notifications_microservice.infrastructure.repositories;

import com.develop.notifications_microservice.domain.models.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryImplTest {

    @Mock
    private JpaNotificationRepository jpaRepository;

    @InjectMocks
    private NotificationRepositoryImpl notificationRepository;

    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = new Notification();
        sampleNotification.setId(1L);
        sampleNotification.setUserId(100L);
        sampleNotification.setDescription("Test Notification");
        sampleNotification.setPurchaseId(999L);
        sampleNotification.setStatus(true);
    }

    @Test
    void testSaveDelegatesToJpaRepository() {
        notificationRepository.save(sampleNotification);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(jpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(sampleNotification.getUserId(), saved.getUserId());
        assertEquals(sampleNotification.getPurchaseId(), saved.getPurchaseId());
        assertEquals(sampleNotification.getDescription(), saved.getDescription());
    }

    @Test
    void testUpdateStatusWhenNotificationExists() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));

        notificationRepository.updateStatus(1L, false);

        assertFalse(sampleNotification.isStatus());
        verify(jpaRepository).save(sampleNotification);
    }

    @Test
    void testUpdateStatusThrowsExceptionWhenNotificationNotFound() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> notificationRepository.updateStatus(1L, true)
        );

        assertEquals("Notification not found with ID: 1", exception.getMessage());
    }

    @Test
    void testFindByUserIdReturnsMappedResults() {
        Notification another = new Notification();
        another.setId(2L);
        another.setUserId(100L);
        another.setDescription("Another Notification");
        another.setPurchaseId(111L);
        another.setStatus(false);

        when(jpaRepository.findByUserId(100L)).thenReturn(List.of(sampleNotification, another));

        List<Notification> result = notificationRepository.findByUserId(100L);

        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getUserId());
        assertEquals(100L, result.get(1).getUserId());
    }
}
