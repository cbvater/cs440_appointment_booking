package uwlax.cs440.appointmentBooking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import uwlax.cs440.appointmentBooking.model.Appointment;
import uwlax.cs440.appointmentBooking.model.ServiceProvider;
import uwlax.cs440.appointmentBooking.repository.AppointmentRepository;
import uwlax.cs440.appointmentBooking.repository.ServiceProviderRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository repo;
    private final ServiceProviderRepository serviceProviderRepository;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository repo, ServiceProviderRepository serviceProviderRepository, NotificationService notificationService) {
        this.repo = repo;
        this.serviceProviderRepository = serviceProviderRepository;
        this.notificationService = notificationService;
    }

    // ------------------------------------------------------------
    // LOOKUP METHODS
    // ------------------------------------------------------------

    public Appointment getById(long id) {
        Optional<Appointment> result = repo.findById(id);
        return result.orElse(null);
    }

    public List<Appointment> getProviderUnbookedSlots(long providerId) {
        return repo.findByServiceProviderIdAndBookedFalseOrderByTimeAsc(providerId);
    }

    public List<Appointment> getCustomerBookings(long userId) {
        return repo.findByUserIdAndBookedTrue(userId);
    }

    public List<Appointment> getCustomerBookingsInOrder(long userId) {
        return repo.findByUserIdAndBookedTrueOrderByTimeAsc(userId);
    }

    public List<Appointment> getProviderBookings(long providerId) {
        return repo.findByServiceProviderIdAndBookedTrueOrderByTimeAsc(providerId);
    }
    /**
     * Finds all appointments unbooked or not
     * Used for admin
     */
    public List<Appointment> getAllAppointments() {
        return repo.findAll();
    }
    /**
     * Open (bookable) slots for the "Schedule Appts" search tab, optionally
     * narrowed to one category. Cancelled slots and slots already in the past
     * never show up here. Soonest slot first.
     */
    public List<Appointment> searchOpenSlots(String category, String providerName,
                                             LocalDate date, LocalTime startTime, LocalTime endTime) {
        return searchOpenSlots(null, category, providerName, date, startTime, endTime);
    }

    /**
     * Same as above, but also hides any slot that overlaps one of this user's
     * active bookings, so they are never offered a time they can't book.
     * Pass a null userId to skip that filter.
     */
    public List<Appointment> searchOpenSlots(Long userId, String category, String providerName,
                                             LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Appointment> open = (category == null || category.isBlank())
                ? repo.findByBookedFalse()
                : repo.findByCategoryIgnoreCaseAndBookedFalse(category);

        LocalDateTime now = LocalDateTime.now();
        Map<Long, ServiceProvider> providers = getProviderProfiles(open);
        String normalizedProviderName = providerName == null ? "" : providerName.trim().toLowerCase(Locale.ROOT);
        List<Appointment> userBookings = userId == null
                ? List.of()
                : repo.findByUserIdAndBookedTrueAndCancelledFalse(userId);

        return open.stream()
                .filter(a -> !a.isCancelled())
                .filter(a -> a.getTime().isAfter(now))
                .filter(a -> date == null || a.getTime().toLocalDate().equals(date))
                .filter(a -> startTime == null || !a.getTime().toLocalTime().isBefore(startTime))
                .filter(a -> endTime == null || !a.getTime().toLocalTime().isAfter(endTime))
                .filter(a -> normalizedProviderName.isBlank()
                        || providerMatches(providers.get(a.getServiceProviderId()), normalizedProviderName))
                .filter(a -> userBookings.stream().noneMatch(booked -> overlaps(booked, a)))
                .sorted(Comparator.comparing(Appointment::getTime))
                .toList();
    }

    /**
     * Same as above searchOpenSlots but for service providers.
     * Hides the appointments the service provider created themselves.
     * Hides appointments that conflict with created appointments.
     * Hides appoinments that conflict with booked appointments
     **/
    public List<Appointment> searchOpenSlotsforProvider(Long userId, String category, String providerName, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Appointment> open = (category == null || category.isBlank())
                ? repo.findByBookedFalse()
                : repo.findByCategoryIgnoreCaseAndBookedFalse(category);

        LocalDateTime now = LocalDateTime.now();
        Map<Long, ServiceProvider> providers = getProviderProfiles(open);
        String normalizedProviderName = providerName == null ? "" : providerName.trim().toLowerCase(Locale.ROOT);

        List<Appointment> providerCreatedAppointments = userId == null
                ? List.of()
                : repo.findByServiceProviderIdAndCancelledFalse(userId);

        List<Appointment> providerBookedAppointments = userId == null
                ? List.of()
                : repo.findByUserIdAndBookedTrueAndCancelledFalse(userId);

        return open.stream()
                .filter(a -> !a.isCancelled())
                .filter(a -> a.getServiceProviderId() != userId)
                .filter(a -> a.getTime().isAfter(now))
                .filter(a -> date == null || a.getTime().toLocalDate().equals(date))
                .filter(a -> startTime == null || !a.getTime().toLocalTime().isBefore(startTime))
                .filter(a -> endTime == null || !a.getTime().toLocalTime().isAfter(endTime))
                .filter(a -> normalizedProviderName.isBlank()
                        || providerMatches(providers.get(a.getServiceProviderId()), normalizedProviderName))
                .filter(a -> providerCreatedAppointments.stream().noneMatch(existing -> overlaps(existing, a)))
                .filter(a -> providerBookedAppointments.stream().noneMatch(existing -> overlaps(existing, a)))
                .sorted(Comparator.comparing(Appointment::getTime))
                .toList();
    }

    public List<Appointment> searchOpenSlots(String category) {
        return searchOpenSlots(category, null, null, null, null);
    }

    public Map<Long, ServiceProvider> getProviderProfiles(List<Appointment> slots) {
        List<Long> providerIds = slots.stream()
                .map(Appointment::getServiceProviderId)
                .distinct()
                .toList();
        Map<Long, ServiceProvider> providers = new HashMap<>();
        serviceProviderRepository.findByUser_UserIdIn(providerIds)
                .forEach(provider -> {
                    providers.put(provider.getId(), provider);
                    if (provider.getUser() != null) {
                        providers.put(provider.getUser().getUserId(), provider);
                    }
                });
        return providers;
    }

    private boolean providerMatches(ServiceProvider provider, String searchTerm) {
        if (provider == null || provider.getUser() == null) {
            return false;
        }
        String providerFullName = (provider.getUser().getFirstName() + " "
                + provider.getUser().getLastName()).toLowerCase(Locale.ROOT);
        return providerFullName.contains(searchTerm);
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

        if (!ServiceCategories.isValid(appt.getCategory())) {
            throw new IllegalArgumentException("Invalid category");
        }

        validateSlotTimes(appt.getTime(), appt.getEndTime(), providerId, 0);

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

        if (!ServiceCategories.isValid(updated.getCategory())) {
            throw new IllegalArgumentException("Invalid category");
        }

        // Re-check the times only when they actually changed, so a provider can
        // still fix a typo in the name or location of a slot that is now in the past.
        boolean timesChanged = !Objects.equals(existing.getTime(), updated.getTime())
                || !Objects.equals(existing.getEndTime(), updated.getEndTime());
        if (timesChanged) {
            validateSlotTimes(updated.getTime(), updated.getEndTime(),
                    existing.getServiceProviderId(), existing.getAppointmentId());
        }

        // Only update editable fields
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setTime(updated.getTime());
        existing.setEndTime(updated.getEndTime());
        existing.setLocation(updated.getLocation());

        return repo.save(existing);
    }

    /** Provider cancels an appointment */
    public Appointment cancelAppointment(long appointmentId, long userId, String actingRole) {
        Appointment appt = getById(appointmentId);
        if (appt == null) return null;

        boolean byProvider = "Service Provider".equals(actingRole);

        appt.setCancelled(true);

        if(byProvider){
            notificationService.notifyUser(appt.getUserId(), "Your appointment \"" + appt.getName() + "\" on " + appt.getTime() + " was cancelled by the provider");
        } else {
            notificationService.notifyUser(appt.getServiceProviderId(), "Your appointment \"" + appt.getName() + "\" on " + appt.getTime() + " was cancelled by the customer");
        }

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

        if (!appt.getTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("That appointment has already started. Please pick another.");
        }

        // Any overlap with one of the user's active bookings is a conflict,
        // not just an identical start time.
        boolean hasConflict = repo.findByUserIdAndBookedTrueAndCancelledFalse(userId).stream()
                .anyMatch(existing -> overlaps(existing, appt));
        if (hasConflict) {
            throw new IllegalStateException("That appointment overlaps one you already have booked.");
        }

        appt.setBooked(true);
        appt.setUserId(userId);

        return repo.save(appt);
    }

    /**
     * Service Provider books a slot.
     *
     * @throws IllegalStateException if the slot no longer exists, was just
     *                                taken by someone else, or overlaps a
     *                                time the user (service provider) already has booked; the
     *                                message is safe to show to the user (service provider).
     */
    public Appointment bookSlotForProvider(long appointmentId, long userId) {
        Appointment appt = getById(appointmentId);
        if (appt == null) {
            throw new IllegalStateException("That appointment no longer exists.");
        }
        if (appt.isBooked() || appt.isCancelled()) {
            throw new IllegalStateException("Sorry, that slot was just taken. Please pick another.");
        }

        if (!appt.getTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("That appointment has already started. Please pick another.");
        }

        // Any overlap with one of the user's active bookings is a conflict,
        // not just an identical start time.
        boolean createdConflict = repo.findByServiceProviderIdAndCancelledFalse(userId).stream()
                .anyMatch(existing -> overlaps(existing, appt));

        boolean bookedConflict = repo.findByUserIdAndBookedTrueAndCancelledFalse(userId).stream()
                .anyMatch(existing -> overlaps(existing, appt));

        if (createdConflict) {
            throw new IllegalStateException("That appointment overlaps one you already have created.");
        }
        if (bookedConflict) {
            throw new IllegalStateException("That appointment overlaps one you already have booked");
        }

        appt.setBooked(true);
        appt.setUserId(userId);

        return repo.save(appt);
    }

    /**
     * Rules for a provider's slot times (used when creating and editing).
     *
     * @param excludeId   appointment to ignore when looking for overlaps (the slot
     *                    being edited); pass 0 when creating a new slot
     * @throws IllegalArgumentException with a message that is safe to show the provider
     */
    private void validateSlotTimes(LocalDateTime start, LocalDateTime end,
                                   long providerId, long excludeId) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Please enter both a start time and an end time.");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("The end time must be after the start time.");
        }
        if (!start.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment slots cannot be created in the past.");
        }

        boolean conflict = repo.findByServiceProviderIdAndCancelledFalse(providerId).stream()
                .filter(other -> other.getAppointmentId() != excludeId)
                .anyMatch(other -> overlaps(other, start, end));
        boolean bookedConflict = repo.findByUserIdAndBookedTrueAndCancelledFalse(providerId).stream()
                .anyMatch(other -> overlaps(other, start, end));
        if (conflict) {
            throw new IllegalArgumentException("That time overlaps another appointment slot you already have.");
        }
        if (bookedConflict) {
            throw new IllegalArgumentException("That time overlaps another appointment you already booked.");
        }
    }

    /** Two appointments overlap if each starts before the other ends. Back-to-back is fine. */
    private boolean overlaps(Appointment a, Appointment b) {
        return overlaps(a, b.getTime(), b.getEndTime());
    }

    private boolean overlaps(Appointment a, LocalDateTime start, LocalDateTime end) {
        return a.getTime().isBefore(end) && start.isBefore(a.getEndTime());
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