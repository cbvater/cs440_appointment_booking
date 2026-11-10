package uwlax.cs440.appointmentBooking.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.util.UriComponentsBuilder;
import uwlax.cs440.appointmentBooking.model.Appointment;
import uwlax.cs440.appointmentBooking.model.ServiceProvider;
import uwlax.cs440.appointmentBooking.service.AppointmentService;
import uwlax.cs440.appointmentBooking.service.NotificationService;
import uwlax.cs440.appointmentBooking.service.ServiceProviderService;
import uwlax.cs440.appointmentBooking.service.NotificationService;

import java.util.List;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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
        List<Appointment> bookedAppointments =
                appointmentService.getCustomerBookingsInOrder(providerId);

        List<Appointment> providerAppointments =
                appointmentService.getProviderBookings(providerId);

        model.addAttribute("bookedAppointments", bookedAppointments);
        model.addAttribute("providerAppointments", providerAppointments);

        return "serviceProviderHome";
    }

    @GetMapping("/serviceProviderSchedAppt")
    public String serviceProviderSchedAppt(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String providerName,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime endTime,
            HttpSession session,
            Model model) {

        if (!"Service Provider".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }

        long providerId = (long) session.getAttribute("userId");

        addProviderSearchResults(providerId, model, category, providerName, date, startTime, endTime);

        return "serviceProviderSchedAppt";
    }

    @PostMapping("/serviceProviderBookSlot/{id}")
    public String bookSlot(@PathVariable long id,
                           @RequestParam(required = false) String category,
                           @RequestParam(required = false) String providerName,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                           LocalDate date,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                           LocalTime startTime,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                           LocalTime endTime,
                           HttpSession session,
                           Model model) {
        if (!"Service Provider".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        long userId = (long) session.getAttribute("userId");

        try {
            appointmentService.bookSlotForProvider(id, userId);
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            addProviderSearchResults(userId, model, category, providerName, date, startTime, endTime);
            return "serviceProviderSchedAppt";
        }

        UriComponentsBuilder redirect = UriComponentsBuilder.fromPath("/serviceProviderSchedAppt")
                .queryParam("booked", "1");
        addQueryParam(redirect, "category", category);
        addQueryParam(redirect, "providerName", providerName);
        addQueryParam(redirect, "date", date);
        addQueryParam(redirect, "startTime", startTime);
        addQueryParam(redirect, "endTime", endTime);
        return "redirect:" + redirect.toUriString();
    }

    private void addProviderSearchResults(long userId, Model model, String category, String providerName,
                                  LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Appointment> slots = appointmentService.searchOpenSlotsforProvider(
                userId, category, providerName, date, startTime, endTime);
        Map<Long, ServiceProvider> providerProfiles = appointmentService.getProviderProfiles(slots);
        model.addAttribute("slots", slots);
        model.addAttribute("providerProfiles", providerProfiles);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("providerName", providerName);
        model.addAttribute("selectedDate", date);
        model.addAttribute("startTime", startTime);
        model.addAttribute("endTime", endTime);
    }

    private void addQueryParam(UriComponentsBuilder builder, String name, Object value) {
        if (value != null && !value.toString().isBlank()) {
            builder.queryParam(name, value);
        }
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
    public String addSlot(@ModelAttribute Appointment appt, HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        long providerId = (long) session.getAttribute("userId");
        try {
            appointmentService.createSlot(appt, providerId);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/serviceProviderEditSlots";
    }

    // ------------------------------------------------------------
    // EDIT EXISTING SLOT (model POST)
    // ------------------------------------------------------------

    @PostMapping("/serviceProviderEditSlot")
    public String editSlot(@ModelAttribute Appointment appt, HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        try {
            appointmentService.editSlot(appt);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
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