package uwlax.cs440.appointmentBooking.controller;

import org.springframework.stereotype.Controller;

/**
 * CONTROLLER LAYER: handles web requests and decides what the user sees next.
 *
 * Called by: the browser (links and form submissions).
 * Calls: UserService (inject it through the constructor).
 *
 * Request flow:
 *   Browser -> UserController -> UserService -> UserRepository -> database
 *   and the result travels back up the same way.
 *
 * What belongs here:
 *  - GET methods that show a page (login, register as user, register as provider)
 *  - POST methods that receive form data (@RequestParam or a form object),
 *    pass it to the service, then redirect or show an error
 *  - Session handling after a successful login
 *
 * What does NOT belong here:
 *  - Password hashing, duplicate checks, or role rules (service)
 *  - Any direct call to UserRepository (always go through the service)
 *
 * Keep each URL in only one controller so mappings never collide.
 * Routes: /login, /register/user, /register/provider
 */
@Controller
public class UserController {

}