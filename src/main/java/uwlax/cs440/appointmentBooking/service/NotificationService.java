package uwlax.cs440.appointmentBooking.service;
import uwlax.cs440.appointmentBooking.repository.NotificationRepository;
import uwlax.cs440.appointmentBooking.model.Notification;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notifyUser(long userId, String message) {
        notificationRepository.save(new Notification(userId, message));
    }

    public List<Notification> getNotifications(long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    public List<Notification> getNewNotifications(long userId) {
        return notificationRepository.findByUserIdAndCheckedFalse(userId);
    }

    public void markAllChecked(long userId) {
        List<Notification> unchecked = notificationRepository.findByUserIdAndCheckedFalse(userId);
        unchecked.forEach(n -> n.setChecked(true));
        notificationRepository.saveAll(unchecked);
    }

    public long countUnchecked(long userId) {
        return notificationRepository.countByUserIdAndCheckedFalse(userId);
    }
}
