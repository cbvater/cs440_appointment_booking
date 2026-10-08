package uwlax.cs440.appointmentBooking.controller;

import org.springframework.stereotype.Controller;

/**
 * CONTROLLER LAYER: handles web requests and decides what the user sees next.
 *
 * Called by: the browser (links and form submissions).
 * Calls: AppointmentService (inject it through the constructor).
 *
 * Request flow:
 *   Browser -> AppointmentController -> AppointmentService -> AppointmentRepository -> database
 *   and the result travels back up the same way.
 *
 * What belongs here:
 *  - GET methods that show a page (browse open slots, a customer's
 *    appointments, a provider's schedule)
 *  - POST methods that receive form data (@RequestParam or a form object),
 *    pass it to the service, then redirect or show an error
 *    (create a slot, book a slot, cancel a booking)
 *
 * What does NOT belong here:
 *  - Booking rules or availability checks (service)
 *  - Any direct call to AppointmentRepository (always go through the service)
 *
 * Keep each URL in only one controller so mappings never collide.
 * Routes: /appointments, /appointments/new, /appointments/book, /appointments/cancel
 */
@Controller
public class AppointmentController {

}