package uwlax.cs440.appointmentBooking.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long appointmentId;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false)
    private long serviceProviderId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(nullable = false)
    private LocalDateTime time;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean booked;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private boolean cancelled;

    public Appointment() {}

    // ⭐ REQUIRED FOR EDITING TO WORK
    public void setAppointmentId(long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public long getAppointmentId() {
        return this.appointmentId;
    }

    public long getUserId() {
        return this.userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getServiceProviderId() {
        return this.serviceProviderId;
    }
    public void setServiceProviderId(long serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public LocalDateTime getTime() {
        return this.time;
    }
    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getCategory() {
        return this.category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public boolean isBooked() {
        return this.booked;
    }
    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public String getLocation() {
        return this.location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}