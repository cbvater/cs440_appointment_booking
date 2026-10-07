package uwlax.cs440.appointmentBooking.model;

import jakarta.persistence.*;
/**
 * MODEL LAYER: describes the data. One User object = one row in the "users" table.
 *
 * Used by: UserRepository (saves and loads it), UserService (creates and
 * checks it), and controllers (only passed along, never changed directly).
 * Calls: nothing. It holds data and has no logic of its own.
 *
 * Both customers, service providers, and admins are stored here. The "role" field
 * tells them apart.
 *
 * What belongs here:
 *  - Fields that map to database columns, with JPA annotations (@Column, etc.)
 *  - A no-argument constructor (JPA requires it)
 *  - Getters and setters for each field
 *
 * What does NOT belong here:
 *  - Password hashing, duplicate checks, or role rules (service)
 *  - Database queries (repository)
 *  - Anything about web requests or pages (controller)
 *
 * Note: the table is named "users" because "user" is a reserved word in Postgres.
 */

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique=true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean active;

    public User() {}

    public String getFirstName(){
        return this.firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public long getUserId(){
        return this.userId;
    }
    public String getLastName(){
        return this.lastName;
    }
    public void setLastName(String lastName){
        this.lastName = lastName;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getRole(){
        return this.role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

}
