package uwlax.cs440.appointmentBooking.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import uwlax.cs440.appointmentBooking.model.Appointment;
import uwlax.cs440.appointmentBooking.model.ServiceProvider;
import uwlax.cs440.appointmentBooking.service.AppointmentService;
import uwlax.cs440.appointmentBooking.service.ServiceProviderService;

import java.util.List;

@Controller
public class ServiceProviderController {

    private final ServiceProviderService serviceProviderService;
    private final AppointmentService appointmentService;

    public ServiceProviderController(ServiceProviderService serviceProviderService,
                                     AppointmentService appointmentService) {
        this.serviceProviderService = serviceProviderService;
        this.appointmentService = appointmentService;
    }

    // ------------------------------------------------------------
    // REGISTRATION
    // ------------------------------------------------------------

    @PostMapping("/register/provider")
    public String handleRegistration(@RequestParam String username,
                                     @RequestParam String firstName,
                                     @RequestParam String lastName,
                                     @RequestParam String company,
                                     @RequestParam String qualification,
                                     @RequestParam String password,
                                     @RequestParam String serviceType,
                                     HttpSession session,
                                     Model model) {
        try {
            ServiceProvider provider = serviceProviderService.register(
                    firstName, lastName, password, username, qualification, company, serviceType
            );

            // Store provider user object
            session.setAttribute("user", provider.getUser());

            // ⭐ CRITICAL FIX: store providerId for appointment lookup
            session.setAttribute("userId", provider.getUser().getUserId());

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("company", company);
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("username", username);
            model.addAttribute("qualification", qualification);
            model.addAttribute("serviceType", serviceType);
            return "serviceProviderRegistration";
        }
        return "redirect:/serviceProviderHome";
    }

    @GetMapping("/register/provider")
    public String showRegistrationForm() {
        return "serviceProviderRegistration";
    }

    // ------------------------------------------------------------
    // PAGE ROUTES
    // ------------------------------------------------------------

    @GetMapping("/serviceProviderHome")
    public String providerHome(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "serviceProviderHome";
    }

    @GetMapping("/serviceProviderSchedAppt")
    public String scheduleAppts(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "serviceProviderSchedAppt";
    }

    @GetMapping("/serviceProviderApptHistory")
    public String appointmentHistory(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "serviceProviderApptHistory";
    }

    // ------------------------------------------------------------
    // EDIT SLOTS PAGE (with appointment list)
    // ------------------------------------------------------------

    @GetMapping("/serviceProviderEditSlots")
    public String editSlots(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        long providerId = (long) session.getAttribute("userId");

        List<Appointment> slots = appointmentService.getProviderSlots(providerId);
        model.addAttribute("slots", slots);

        return "serviceProviderEditSlots";
    }

    // ------------------------------------------------------------
    // ADD NEW SLOT (modal POST)
    // ------------------------------------------------------------

    @PostMapping("/serviceProviderAddSlot")
    public String addSlot(@ModelAttribute Appointment appt, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        long providerId = (long) session.getAttribute("userId");
        appointmentService.createSlot(appt, providerId);

        return "redirect:/serviceProviderEditSlots";
    }

    // ------------------------------------------------------------
    // EDIT EXISTING SLOT (model POST)
    // ------------------------------------------------------------

    @PostMapping("/serviceProviderEditSlot")
    public String editSlot(@ModelAttribute Appointment appt, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        appointmentService.editSlot(appt);
        return "redirect:/serviceProviderEditSlots";
    }

    // ------------------------------------------------------------
    // CANCEL SLOT
    // ------------------------------------------------------------

    @PostMapping("/serviceProviderCancelSlot/{id}")
    public String cancelSlot(@PathVariable long id, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        appointmentService.cancelSlot(id);
        return "redirect:/serviceProviderEditSlots";
    }

    // ------------------------------------------------------------
    // UNCANCEL SLOT
    // ------------------------------------------------------------

    @PostMapping("/serviceProviderUncancelSlot/{id}")
    public String uncancelSlot(@PathVariable long id, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        appointmentService.uncancelSlot(id);
        return "redirect:/serviceProviderEditSlots";
    }

}