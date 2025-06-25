package com.develop.notifications_microservice.interfaces;

import com.develop.notifications_microservice.domain.models.Notification;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        Notification notif = new Notification();
        assertNotNull(notif);
        assertNull(notif.getId());
        assertFalse(notif.isStatus()); // valor por defecto de boolean
    }

    @Test
    void allArgsConstructor_shouldAssignAllFields() {
        LocalDateTime now = LocalDateTime.now();

        Notification notif = new Notification(
                1L,
                100L,
                "Descripción",
                200L,
                true,
                now,
                "Título"
        );

        assertEquals(1L, notif.getId());
        assertEquals(100L, notif.getUserId());
        assertEquals("Descripción", notif.getDescription());
        assertEquals(200L, notif.getPurchaseId());
        assertTrue(notif.isStatus());
        assertEquals(now, notif.getCreatedOn());
        assertEquals("Título", notif.getTitle());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        Notification notif = new Notification();
        notif.setId(10L);
        notif.setUserId(99L);
        notif.setPurchaseId(555L);
        notif.setDescription("Test desc");
        notif.setTitle("Test title");
        notif.setStatus(false);
        LocalDateTime time = LocalDateTime.now();
        notif.setCreatedOn(time);

        assertEquals(10L, notif.getId());
        assertEquals(99L, notif.getUserId());
        assertEquals(555L, notif.getPurchaseId());
        assertEquals("Test desc", notif.getDescription());
        assertEquals("Test title", notif.getTitle());
        assertFalse(notif.isStatus());
        assertEquals(time, notif.getCreatedOn());
    }

    @Test
    void onCreate_shouldSetCreatedOnToNow() throws Exception {
        Notification notif = new Notification();

        Method method = Notification.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(notif);

        LocalDateTime now = LocalDateTime.now();
        assertNotNull(notif.getCreatedOn());
        assertTrue(Math.abs(now.getSecond() - notif.getCreatedOn().getSecond()) <= 1);
    }



    @Test
    void toString_shouldContainKeyFields() {
        Notification notif = new Notification();
        notif.setId(7L);
        notif.setTitle("Alerta");
        notif.setDescription("Prueba");

        String result = notif.toString();
        assertTrue(result.contains("7"));
        assertTrue(result.contains("Alerta") || result.contains("Prueba"));
    }

    @Test
    void equalsAndHashCode_shouldWorkForSameData() {
        LocalDateTime now = LocalDateTime.now();
        Notification notif1 = new Notification(1L, 10L, "desc", 20L, true, now, "title");
        Notification notif2 = new Notification(1L, 10L, "desc", 20L, true, now, "title");

        assertEquals(notif1, notif2);
        assertEquals(notif1.hashCode(), notif2.hashCode());
    }
}
