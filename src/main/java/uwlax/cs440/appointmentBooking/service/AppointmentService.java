package uwlax.cs440.appointmentBooking.service;

import org.springframework.stereotype.Service;

/**
 * SERVICE LAYER: the business logic. The "rules" of the app live here.
 *
 * Called by: AppointmentController.
 * Calls: AppointmentRepository (inject it through the constructor).
 *
 * What belongs here:
 *  - Creating an open slot (a provider sets time, category, name, location;
 *    booked starts false)
 *  - Booking a slot (set the customer's userId on it, flip booked to true;
 *    reject if the slot doesn't exist or is already booked)
 *  - Cancelling a booking (clear userId, flip booked back to false)
 *  - Listing: a provider's open slots, a customer's booked appointments,
 *    a provider's full schedule, slots by category
 *
 * What does NOT belong here:
 *  - Reading request parameters, choosing which page to show, redirects
 *    (that is the controller's job)
 *  - Writing SQL or query logic (that is the repository's job)
 *
 * Tip: throw an exception or return an empty result when something fails
 * (e.g. slot already booked), and let the controller decide what the user sees.
 */
@Service
public class AppointmentService {

}