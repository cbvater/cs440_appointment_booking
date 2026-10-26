package uwlax.cs440.appointmentBooking.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import uwlax.cs440.appointmentBooking.model.Notification;
import uwlax.cs440.appointmentBooking.model.User;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(long userId);
    List<Notification> findByUserIdAndCheckedFalse(long userId);
    long countByUserIdAndCheckedFalse(long userId);
}
