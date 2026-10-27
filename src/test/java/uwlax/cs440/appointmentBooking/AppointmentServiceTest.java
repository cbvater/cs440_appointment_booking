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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
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


    // ------------------------------------------------------------
    // PROVIDER: creating slots
    // ------------------------------------------------------------

    @Test
    void createSlotRejectsStartInThePast() {
        AppointmentService service = newService();
        LocalDateTime start = LocalDateTime.now().minusHours(2);

        assertThatThrownBy(() -> service.createSlot(newSlot(start, start.plusHours(1)), 7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("past");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createSlotRejectsEndBeforeOrEqualToStart() {
        AppointmentService service = newService();
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> service.createSlot(newSlot(start, start), 7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("end time");
        assertThatThrownBy(() -> service.createSlot(newSlot(start, start.minusMinutes(5)), 7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("end time");
    }

    @Test
    void createSlotRejectsMissingEndTime() {
        AppointmentService service = newService();

        assertThatThrownBy(() -> service.createSlot(newSlot(LocalDateTime.now().plusDays(1), null), 7))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createSlotRejectsOverlapWithExistingSlot() {
        AppointmentService service = newService();
        LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Appointment existing = slot(base, base.plusHours(1), 7);
        existing.setAppointmentId(1);
        when(appointmentRepository.findByServiceProviderIdAndCancelledFalse(7))
                .thenReturn(List.of(existing));

        // starts inside, ends inside, fully contains, identical
        for (LocalDateTime[] t : new LocalDateTime[][]{
                {base.plusMinutes(30), base.plusMinutes(90)},
                {base.minusMinutes(30), base.plusMinutes(30)},
                {base.minusHours(1), base.plusHours(2)},
                {base, base.plusHours(1)}}) {
            assertThatThrownBy(() -> service.createSlot(newSlot(t[0], t[1]), 7))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("overlaps");
        }
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createSlotAllowsBackToBackSlots() {
        AppointmentService service = newService();
        LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Appointment existing = slot(base, base.plusHours(1), 7);
        existing.setAppointmentId(1);
        when(appointmentRepository.findByServiceProviderIdAndCancelledFalse(7))
                .thenReturn(List.of(existing));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        assertThatCode(() -> {
            service.createSlot(newSlot(base.plusHours(1), base.plusHours(2)), 7);   // right after
            service.createSlot(newSlot(base.minusHours(1), base), 7);               // right before
        }).doesNotThrowAnyException();
    }

    // ------------------------------------------------------------
    // PROVIDER: editing slots
    // ------------------------------------------------------------

    @Test
    void editSlotIgnoresItselfWhenCheckingOverlap() {
        AppointmentService service = newService();
        LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Appointment existing = slot(base, base.plusHours(1), 7);
        existing.setAppointmentId(5);
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.findByServiceProviderIdAndCancelledFalse(7))
                .thenReturn(List.of(existing));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        Appointment edit = newSlot(base, base.plusMinutes(90)); // extend the same slot
        edit.setAppointmentId(5);

        assertThat(service.editSlot(edit).getEndTime()).isEqualTo(base.plusMinutes(90));
    }

    @Test
    void editSlotRejectsMovingIntoThePastOrOntoAnotherSlot() {
        AppointmentService service = newService();
        LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Appointment mine = slot(base, base.plusHours(1), 7);
        mine.setAppointmentId(5);
        Appointment other = slot(base.plusHours(3), base.plusHours(4), 7);
        other.setAppointmentId(6);
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(mine));
        when(appointmentRepository.findByServiceProviderIdAndCancelledFalse(7))
                .thenReturn(List.of(mine, other));

        Appointment toPast = newSlot(LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1).plusHours(1));
        toPast.setAppointmentId(5);
        assertThatThrownBy(() -> service.editSlot(toPast))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("past");

        Appointment ontoOther = newSlot(base.plusHours(3).plusMinutes(30), base.plusHours(5));
        ontoOther.setAppointmentId(5);
        assertThatThrownBy(() -> service.editSlot(ontoOther))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("overlaps");
        verify(appointmentRepository, never()).save(any());
    }

    // ------------------------------------------------------------
    // CUSTOMER: booking
    // ------------------------------------------------------------

    @Test
    void bookSlotRejectsOverlapWithUsersExistingBooking() {
        AppointmentService service = newService();
        LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Appointment booked = slot(base, base.plusHours(1), 7);
        booked.setBooked(true);
        booked.setUserId(42);
        Appointment wanted = slot(base.plusMinutes(30), base.plusMinutes(90), 8); // different provider
        wanted.setAppointmentId(9);
        when(appointmentRepository.findById(9L)).thenReturn(Optional.of(wanted));
        when(appointmentRepository.findByUserIdAndBookedTrueAndCancelledFalse(42))
                .thenReturn(List.of(booked));

        assertThatThrownBy(() -> service.bookSlot(9, 42))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("overlaps");
        assertThat(wanted.isBooked()).isFalse();
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void bookSlotAllowsBackToBackAndMarksBooked() {
        AppointmentService service = newService();
        LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Appointment booked = slot(base, base.plusHours(1), 7);
        booked.setBooked(true);
        booked.setUserId(42);
        Appointment wanted = slot(base.plusHours(1), base.plusHours(2), 8);
        wanted.setAppointmentId(9);
        when(appointmentRepository.findById(9L)).thenReturn(Optional.of(wanted));
        when(appointmentRepository.findByUserIdAndBookedTrueAndCancelledFalse(42))
                .thenReturn(List.of(booked));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        Appointment result = service.bookSlot(9, 42);

        assertThat(result.isBooked()).isTrue();
        assertThat(result.getUserId()).isEqualTo(42);
    }

    @Test
    void bookSlotRejectsSlotThatAlreadyStarted() {
        AppointmentService service = newService();
        Appointment past = slot(LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(50), 8);
        past.setAppointmentId(9);
        when(appointmentRepository.findById(9L)).thenReturn(Optional.of(past));

        assertThatThrownBy(() -> service.bookSlot(9, 42))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already started");
    }

    @Test
    void searchOpenSlotsHidesSlotsThatOverlapTheUsersBookings() {
        LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Appointment booked = slot(base, base.plusHours(1), 7);
        booked.setBooked(true);
        booked.setUserId(42);
        Appointment sameStart = slot(base, base.plusMinutes(30), 8);
        Appointment partialOverlap = slot(base.plusMinutes(30), base.plusMinutes(90), 8);
        Appointment backToBack = slot(base.plusHours(1), base.plusHours(2), 8);
        Appointment earlier = slot(base.minusHours(2), base.minusHours(1), 8);

        when(appointmentRepository.findByBookedFalse())
                .thenReturn(List.of(sameStart, partialOverlap, backToBack, earlier));
        when(appointmentRepository.findByUserIdAndBookedTrueAndCancelledFalse(42))
                .thenReturn(List.of(booked));
        when(serviceProviderRepository.findByUser_UserIdIn(any())).thenReturn(List.of());

        List<Appointment> results = newService()
                .searchOpenSlots(42L, null, null, null, null, null);

        assertThat(results).containsExactly(earlier, backToBack);
    }

    private AppointmentService newService() {
        return new AppointmentService(appointmentRepository, serviceProviderRepository, notificationService);
    }

    private Appointment newSlot(LocalDateTime start, LocalDateTime end) {
        Appointment a = slot(start, end, 0);
        a.setName("Checkup");
        a.setCategory("Medical");
        a.setLocation("Room 1");
        return a;
    }

    private Appointment slot(LocalDateTime start, LocalDateTime end, long providerId) {
        Appointment a = slot(start, providerId);
        a.setEndTime(end);
        return a;
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
