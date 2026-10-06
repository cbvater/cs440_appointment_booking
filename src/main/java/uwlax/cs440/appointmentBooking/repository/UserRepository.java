package uwlax.cs440.appointmentBooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uwlax.cs440.appointmentBooking.model.User;

import java.util.List;

/**
 * REPOSITORY LAYER: talks to the database, and nothing else.
 *
 * Called by: UserService (never by controllers directly).
 * Calls: nothing. Spring generates the implementation at startup.
 *
 * JpaRepository<User, Long> already gives us save, findById, findAll,
 * delete, and so on, so only add methods the defaults don't cover.
 *
 * What belongs here:
 *  - Lookup methods named after the field, which Spring turns into queries
 *    (e.g. findByUsername, existsByUsername, findByRole)
 *  - Custom queries with @Query, if a method name gets too awkward
 *
 * What does NOT belong here:
 *  - Business rules (duplicate checks, password hashing, role logic)
 *  - Anything about web requests or pages
 */
public interface UserRepository extends JpaRepository<User, Long> {

}