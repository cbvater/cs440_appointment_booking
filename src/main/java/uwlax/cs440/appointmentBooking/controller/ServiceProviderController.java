package uwlax.cs440.appointmentBooking.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import uwlax.cs440.appointmentBooking.model.Appointment;
import uwlax.cs440.appointmentBooking.model.ServiceProvider;
import uwlax.cs440.appointmentBooking.service.AppointmentService;
import uwlax.cs440.appointmentBooking.service.NotificationService;
import uwlax.cs440.appointmentBooking.service.ServiceProviderService;
import uwlax.cs440.appointmentBooking.service.NotificationService;

import java.util.List;

@Controller
public class ServiceProviderController {

    private final ServiceProviderService serviceProviderService;
    private final AppointmentService appointmentService;
    private final NotificationService notificationService;

    public ServiceProviderController(ServiceProviderService serviceProviderService,
                                     AppointmentService appointmentService,
                                     NotificationService notificationService) {
        this.serviceProviderService = serviceProviderService;
        this.appointmentService = appointmentService;
        this.notificationService = notificationService;
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
    public String providerHome(HttpSession session, Model model) {
        System.out.println("role=" + session.getAttribute("role")
                + " userId=" + session.getAttribute("userId"));

        if (!"Service Provider".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }

        long providerId = (long) session.getAttribute("userId");
        model.addAttribute("appointments", appointmentService.getProviderBookings(providerId));

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

        List<Appointment> slots = appointmentService.getProviderUnbookedSlots(providerId);
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
    // DELETE SLOT
    // ------------------------------------------------------------
    // Service provider can delete available appointment slots; it deletes the appointment from the DB
    // Service provider can cancel booked appointments(which sets appointment to "canceled" in DB); that method is cancelAppt();
    @PostMapping("/serviceProviderDeleteSlot/{id}")
    public String cancelSlot(@PathVariable long id, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        appointmentService.delete(id);
        return "redirect:/serviceProviderEditSlots";
    }


    // ------------------------------------------------------------
    // CANCEL BOOKED APPT
    // ------------------------------------------------------------
    @PostMapping("/serviceProviderCancelAppt/{id}")
    public String cancelAppt(@PathVariable long id, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        long userId = (long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        appointmentService.cancelAppointment(id, userId, role);
        return "redirect:/serviceProviderHome";
    }

    // ------------------------------------------------------------
    // NOTIFICATIONS
    // ------------------------------------------------------------

    @GetMapping("/serviceProviderNotifications")
    public String notifications(HttpSession session, Model model) {
        if (!"Service Provider".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        long userId = (long) session.getAttribute("userId");
        model.addAttribute("notifications", notificationService.getNotifications(userId));
        return "serviceProviderNotifications";
    }
    @PostMapping("/serviceProviderNotifications/markChecked")
    public String markSeen(HttpSession session) {
        if (!"Service Provider".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        notificationService.markAllChecked((long) session.getAttribute("userId"));
        return "redirect:/serviceProviderNotifications";
    }
}