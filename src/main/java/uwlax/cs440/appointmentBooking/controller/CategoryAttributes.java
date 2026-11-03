package uwlax.cs440.appointmentBooking.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import uwlax.cs440.appointmentBooking.service.ServiceCategories;

import java.util.List;

/**
 * Makes the category list available to every template as ${categories}, so the
 * registration page, the slot forms and the customer search all build their
 * dropdowns from the same source.
 */
@ControllerAdvice
public class CategoryAttributes {

    @ModelAttribute("categories")
    public List<String> categories() {
        return ServiceCategories.ALL;
    }
}
