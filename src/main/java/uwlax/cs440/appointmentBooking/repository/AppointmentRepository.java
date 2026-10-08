package uwlax.cs440.appointmentBooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uwlax.cs440.appointmentBooking.model.Appointment;

import java.util.List;

/**
 * REPOSITORY LAYER: talks to the database, and nothing else.
 *
 * Called by: AppointmentService (never by controllers directly).
 * Calls: nothing. Spring generates the implementation at startup.
 *
 * JpaRepository<Appointment, Long> already gives us save, findById,
 * findAll, delete, and so on, so only add methods the defaults don't cover.
 *
 * What belongs here:
 *  - Lookup methods named after the field, which Spring turns into queries
 *    (e.g. findByUserId, findByServiceProviderId, findByBookedFalse)
 *  - Custom queries with @Query, if a method name gets too awkward
 *
 * What does NOT belong here:
 *  - Business rules (booking conflicts, availability windows)
 *  - Anything about web requests or pages
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // All appointments tied to one customer (booked and/or historical)
    List<Appointment> findByUserId(long userId);

    // Everything on one provider's schedule, booked and open
    List<Appointment> findByServiceProviderId(long serviceProviderId);

    // A provider's open slots only (booked = false) — what customers browse
    List<Appointment> findByServiceProviderIdAndBookedFalse(long serviceProviderId);

    // A customer's confirmed bookings only (booked = true)
    List<Appointment> findByUserIdAndBookedTrue(long userId);

    // Every open slot across all providers, e.g. for a search/browse page
    List<Appointment> findByBookedFalse();

    // Open slots in a given category, e.g. for filtering a browse page
    List<Appointment> findByCategoryAndBookedFalse(String category);
}