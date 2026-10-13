package uwlax.cs440.appointmentBooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uwlax.cs440.appointmentBooking.model.ServiceProvider;
import uwlax.cs440.appointmentBooking.model.User;

import java.util.List;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    List<ServiceProvider> findByUser_UserIdIn(List<Long> userIds);
}
