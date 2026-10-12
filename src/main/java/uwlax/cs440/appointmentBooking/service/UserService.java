package uwlax.cs440.appointmentBooking.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uwlax.cs440.appointmentBooking.model.User;
import uwlax.cs440.appointmentBooking.repository.UserRepository;
import java.util.Optional;

/**
 * SERVICE LAYER: the business logic. The "rules" of the app live here.
 *
 * Called by: UserController.
 * Calls: UserRepository (inject it through the constructor).
 *
 * What belongs here:
 *  - Registering users and providers (set the role, hash the password with
 *    BCrypt, reject usernames that already exist, set active = true)
 *  - Authenticating a login (find by username, compare the password to the hash)
 *  - Any check that decides whether something is allowed
 *
 * What does NOT belong here:
 *  - Reading request parameters, choosing which page to show, redirects
 *    (that is the controller's job)
 *  - Writing SQL or query logic (that is the repository's job)
 *
 * Tip: throw an exception or return an empty result when something fails
 * (e.g. username taken), and let the controller decide what the user sees.
 */
@Service
public class UserService {

    // Password must be more than 7 characters
    private static final int MIN_PASSWORD_LENGTH = 7;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> userLogin(String username, String password){
        Optional<User> found = userRepository.findByUsername(username);

        if (found.isPresent()
                && passwordEncoder.matches(password, found.get().getPasswordHash())) {
            return found;
        }
        return Optional.empty();
    }

    /**
     * Registers a new customer account.
     *
     * @throws IllegalArgumentException if the password is too short or the
     *                                  username is already taken; the message
     *                                  is written to be shown to the user as-is.
     */
    public User registerUser(String firstName, String lastName, String username, String password) {
        if (password == null || password.length() <= MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be more than 7 characters.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("That username is already taken.");
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setActive(true);

        return userRepository.save(user);
    }


}
