package uwlax.cs440.appointmentBooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uwlax.cs440.appointmentBooking.model.Appointment;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // All appointments tied to one customer (booked and/or historical)
    List<Appointment> findByUserId(long userId);

    // Everything on one provider's schedule, booked and open
    List<Appointment> findByServiceProviderId(long serviceProviderId);

    // A provider's open slots only (booked = false) — what customers browse
    List<Appointment> findByServiceProviderIdAndBookedFalse(long serviceProviderId);

    // A customer's confirmed bookings only (booked = true)
    List<Appointment> findByUserIdAndBookedTrue(long userId);

    // Customers full list of bookings in order
    List<Appointment> findByUserIdAndBookedTrueOrderByTimeAsc(long userId);

    // Every open slot across all providers, e.g. for a search/browse page
    List<Appointment> findByBookedFalse();

    // Open slots in a given category, e.g. for filtering a browse page
    List<Appointment> findByCategoryAndBookedFalse(String category);

    // Same as above, but tolerant of how a provider typed the category
    // (e.g. "medical" vs "Medical") when the user searches by it
    List<Appointment> findByCategoryIgnoreCaseAndBookedFalse(String category);

    // Provider's unbooked appts sorted chronologically
    List<Appointment> findByServiceProviderIdAndBookedFalseOrderByTimeAsc(long serviceProviderId);

    // Providers upcoming appointments
    List<Appointment> findByServiceProviderIdAndBookedTrueOrderByTimeAsc(Long serviceProviderId);


    // Everything on a provider's schedule that is still active (not cancelled),
    // booked or open. Used to detect overlapping slots.
    List<Appointment> findByServiceProviderIdAndCancelledFalse(long serviceProviderId);

    // A customer's active bookings (booked and not cancelled by the provider).
    // Used to detect double-booking.
    List<Appointment> findByUserIdAndBookedTrueAndCancelledFalse(long userId);

}
