package uwlax.cs440.appointmentBooking.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * MODEL LAYER: describes the data. One Appointment object = one row in the
 * "appointments" table.
 *
 * Used by: AppointmentRepository (saves and loads it), AppointmentService
 * (creates and checks it), and controllers (only passed along, never
 * changed directly).
 * Calls: nothing. It holds data and has no logic of its own.
 *
 * Covers both past/completed appointments and open slots a service
 * provider has made available, per the ER diagram. The "booked" flag
 * tells them apart: false = an open slot a customer can reserve,
 * true = already taken.
 *
 * What belongs here:
 *  - Fields that map to database columns, with JPA annotations (@Column, etc.)
 *  - A no-argument constructor (JPA requires it)
 *  - Getters and setters for each field
 *
 * What does NOT belong here:
 *  - Booking rules, conflict checks, or availability logic (service)
 *  - Database queries (repository)
 *  - Anything about web requests or pages (controller)
 *
 * Note: userId and serviceProviderId are stored as plain foreign-key ids
 * (matching User.userId, and the future ServiceProvider.serviceProviderId
 * from the ER diagram's separate Service Provider table) rather than
 * @ManyToOne relationships, since the ServiceProvider entity hasn't been
 * built yet. Once it exists, these can be converted to real JPA relations.
 */
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

    public Appointment() {}

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
}
