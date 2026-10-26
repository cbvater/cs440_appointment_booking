package uwlax.cs440.appointmentBooking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uwlax.cs440.appointmentBooking.model.Appointment;
import uwlax.cs440.appointmentBooking.model.ServiceProvider;
import uwlax.cs440.appointmentBooking.model.User;
import uwlax.cs440.appointmentBooking.repository.AppointmentRepository;
import uwlax.cs440.appointmentBooking.repository.ServiceProviderRepository;
import uwlax.cs440.appointmentBooking.service.AppointmentService;
import uwlax.cs440.appointmentBooking.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceProviderRepository serviceProviderRepository;

    @Mock
    private NotificationService notificationService;

    @Test
    void searchOpenSlotsFiltersByProviderNameAndDate() {
        LocalDate appointmentDate = LocalDate.now().plusDays(1);
        Appointment matchingSlot = slot(appointmentDate.atTime(10, 0), 7);
        Appointment differentProvider = slot(appointmentDate.atTime(11, 0), 8);
        ServiceProvider matchingProvider = provider(7, "Jane", "Doe");
        ServiceProvider otherProvider = provider(8, "Alex", "Smith");

        when(appointmentRepository.findByBookedFalse())
                .thenReturn(List.of(differentProvider, matchingSlot));
        when(serviceProviderRepository.findByUser_UserIdIn(any()))
                .thenReturn(List.of(matchingProvider, otherProvider));

        AppointmentService service = new AppointmentService(appointmentRepository, serviceProviderRepository, notificationService);

        List<Appointment> results = service.searchOpenSlots(
                null, "jane", appointmentDate, null, null);

        assertThat(results).containsExactly(matchingSlot);
    }

    private Appointment slot(LocalDateTime time, long providerId) {
        Appointment appointment = new Appointment();
        appointment.setTime(time);
        appointment.setServiceProviderId(providerId);
        appointment.setBooked(false);
        appointment.setCancelled(false);
        return appointment;
    }

    private ServiceProvider provider(long id, String firstName, String lastName) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);

        ServiceProvider provider = new ServiceProvider();
        provider.setId(id);
        provider.setUser(user);
        return provider;
    }
}
