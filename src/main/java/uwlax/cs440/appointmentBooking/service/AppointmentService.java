package uwlax.cs440.appointmentBooking.service;

import org.springframework.stereotype.Service;
import uwlax.cs440.appointmentBooking.model.Appointment;
import uwlax.cs440.appointmentBooking.repository.AppointmentRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository repo;

    public AppointmentService(AppointmentRepository repo) {
        this.repo = repo;
    }

    // ------------------------------------------------------------
    // LOOKUP METHODS
    // ------------------------------------------------------------

    public Appointment getById(long id) {
        Optional<Appointment> result = repo.findById(id);
        return result.orElse(null);
    }

    public List<Appointment> getProviderSlots(long providerId) {
        return repo.findByServiceProviderIdOrderByTimeAsc(providerId);
    }

    public List<Appointment> getCustomerBookings(long userId) {
        return repo.findByUserIdAndBookedTrue(userId);
    }

    /**
     * Open (bookable) slots for the "Schedule Appts" search tab, optionally
     * narrowed to one category. Cancelled slots and slots already in the past
     * never show up here. Soonest slot first.
     */
    public List<Appointment> searchOpenSlots(String category) {
        List<Appointment> open = (category == null || category.isBlank())
                ? repo.findByBookedFalse()
                : repo.findByCategoryIgnoreCaseAndBookedFalse(category);

        LocalDateTime now = LocalDateTime.now();
        return open.stream()
                .filter(a -> !a.isCancelled())
                .filter(a -> a.getTime().isAfter(now))
                .sorted(Comparator.comparing(Appointment::getTime))
                .toList();
    }

    // ------------------------------------------------------------
    // CREATE / UPDATE / DELETE
    // ------------------------------------------------------------

    public Appointment save(Appointment appt) {
        return repo.save(appt);
    }

    public void delete(long id) {
        repo.deleteById(id);
    }

    // ------------------------------------------------------------
    // BUSINESS LOGIC
    // ------------------------------------------------------------

    /** Provider creates a new open slot */
    public Appointment createSlot(Appointment appt, long providerId) {
        appt.setServiceProviderId(providerId);
        appt.setBooked(false);
        appt.setCancelled(false);
        appt.setUserId(0);
        return repo.save(appt);
    }

    /** Provider edits an existing slot (safe merge) */
    public Appointment editSlot(Appointment updated) {
        Appointment existing = getById(updated.getAppointmentId());
        if (existing == null) return null;

        // Only update editable fields
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setTime(updated.getTime());
        existing.setLocation(updated.getLocation());

        return repo.save(existing);
    }

    /** Provider cancels a slot */
    public Appointment cancelSlot(long id) {
        Appointment appt = getById(id);
        if (appt == null) return null;

        appt.setCancelled(true);
        appt.setBooked(false);
        appt.setUserId(0);

        return repo.save(appt);
    }

    /** Provider un-cancels a slot */
    public Appointment uncancelSlot(long id) {
        Appointment appt = getById(id);
        if (appt == null) return null;

        appt.setCancelled(false);
        return repo.save(appt);
    }

    /**
     * Customer books a slot.
     *
     * @throws IllegalStateException if the slot no longer exists, was just
     *                                taken by someone else, or overlaps a
     *                                time the user already has booked; the
     *                                message is safe to show to the user.
     */
    public Appointment bookSlot(long appointmentId, long userId) {
        Appointment appt = getById(appointmentId);
        if (appt == null) {
            throw new IllegalStateException("That appointment no longer exists.");
        }
        if (appt.isBooked() || appt.isCancelled()) {
            throw new IllegalStateException("Sorry, that slot was just taken. Please pick another.");
        }

        boolean hasConflict = repo.findByUserIdAndBookedTrue(userId).stream()
                .anyMatch(existing -> existing.getTime().equals(appt.getTime()));
        if (hasConflict) {
            throw new IllegalStateException("You already have an appointment booked at that time.");
        }

        appt.setBooked(true);
        appt.setUserId(userId);

        return repo.save(appt);
    }

    /** Customer cancels their booking */
    public Appointment customerCancel(long appointmentId) {
        Appointment appt = getById(appointmentId);
        if (appt == null) return null;

        appt.setBooked(false);
        appt.setUserId(0);
        appt.setCancelled(true);

        return repo.save(appt);
    }
}