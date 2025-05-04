package com.develop.notifications_microservice.infrastructure.repositories;
import com.develop.notifications_microservice.domain.interfaces.NotificationPersistencePort;
import com.develop.notifications_microservice.domain.models.Notification;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public class NotificationRepositoryImpl implements NotificationPersistencePort {

    private final JpaNotificationRepository jpaRepo;

    public NotificationRepositoryImpl(JpaNotificationRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Notification save(Notification notification) {
        Notification entity = toEntity(notification);
        jpaRepo.save(entity);
        return notification;
    }

    @Override
    public void updateStatus(Long id, boolean status) {
        jpaRepo.findById(id).ifPresentOrElse(entity -> {
            entity.setStatus(status);
            jpaRepo.save(entity);
        }, () -> {
            throw new IllegalArgumentException("Notification not found with ID: " + id);
        });
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        return jpaRepo.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Notification toEntity(Notification n) {
        Notification e = new Notification();
        e.setId(n.getId());
        e.setUserId(n.getUserId());
        e.setDescription(n.getDescription());
        e.setPurchaseId(n.getPurchaseId());
        e.setStatus(n.isStatus());
        return e;
    }

    private Notification toDomain(Notification e) {
        Notification n = new Notification();
        n.setId(e.getId());
        n.setUserId(e.getUserId());
        n.setDescription(e.getDescription());
        n.setPurchaseId(e.getPurchaseId());
        n.setStatus(e.isStatus());
        return n;
    }
}
