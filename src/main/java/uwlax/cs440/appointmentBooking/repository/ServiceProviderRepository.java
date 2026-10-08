package uwlax.cs440.appointmentBooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uwlax.cs440.appointmentBooking.model.ServiceProvider;
import uwlax.cs440.appointmentBooking.model.User;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
}
