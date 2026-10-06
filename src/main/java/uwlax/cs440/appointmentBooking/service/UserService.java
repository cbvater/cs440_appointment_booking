package uwlax.cs440.appointmentBooking.service;

import org.springframework.stereotype.Service;

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

}