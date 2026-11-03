package uwlax.cs440.appointmentBooking.service;

import java.util.List;

/**
 * The one list of service categories used across the app.
 *
 * Used by:
 *  - the provider registration "Service Type" dropdown
 *  - the provider add/edit slot "Category" dropdowns
 *  - the customer "Schedule Appts" search filter
 *  - server-side validation in AppointmentService and ServiceProviderService
 *
 * To add a category, add it here and nothing else needs to change.
 * (CategoryAttributes exposes this list to every Thymeleaf template as ${categories}.)
 */
public final class ServiceCategories {

    public static final List<String> ALL = List.of("Beauty", "Fitness", "Medical");

    private ServiceCategories() {}

    /** True only for an exact match with one of the known categories (null-safe). */
    public static boolean isValid(String category) {
        return category != null && ALL.contains(category);
    }
}
