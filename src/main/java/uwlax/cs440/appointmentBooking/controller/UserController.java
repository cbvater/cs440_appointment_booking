package uwlax.cs440.appointmentBooking.controller;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import uwlax.cs440.appointmentBooking.model.Appointment;
import uwlax.cs440.appointmentBooking.model.ServiceProvider;
import uwlax.cs440.appointmentBooking.model.User;
import uwlax.cs440.appointmentBooking.service.AppointmentService;
import uwlax.cs440.appointmentBooking.service.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private final UserService userService;
    private final AppointmentService appointmentService;

    public UserController(UserService userService, AppointmentService appointmentService){
        this.userService = userService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/register/user")
    public String showUserRegistration() {
        return "userRegistration";
    }

    @PostMapping("/register/user")
    public String registerUser(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String username,
                               @RequestParam String password,
                               Model model) {
        try {
            userService.registerUser(firstName, lastName, username, password);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("username", username);
            return "userRegistration";
        }
        return "redirect:/login";
    }


    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // renders templates/login.html
    }

    @PostMapping("/login")
    public String handleUserLogin(@RequestParam String username,
                                  @RequestParam String password,
                                  HttpSession session,
                                  Model model) {
        Optional<User> user = userService.userLogin(username, password);
        if(user.isPresent()) {
            session.setAttribute("user", user.get());
            session.setAttribute("userId", user.get().getUserId());
            session.setAttribute("role", user.get().getRole());

            if(user.get().getRole().equals("User")){
                return "redirect:/userHome";
            }
            if(user.get().getRole().equals("Service Provider")){
                return "redirect:/serviceProviderHome";
            }
            if(user.get().getRole().equals("Admin")){
                return "redirect:/adminHome";
            }
        }
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    @GetMapping("/userHome")
    public String showHome(HttpSession session) {
        if (session.getAttribute("userId") == null)
        {
            return "redirect:/login"; // not logged in
        }
        return "userHome";
    }

    @GetMapping("/adminHome")
    public String showAdminHome(HttpSession session) {
        if (!"Admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        return "adminHome";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/userSchedAppt")
    public String userSchedAppt(@RequestParam(required = false) String category,
                                @RequestParam(required = false) String providerName,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                LocalDate date,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                                LocalTime startTime,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                                LocalTime endTime,
                                HttpSession session,
                                Model model) {
        if (!"User".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }

        addSearchResults(model, category, providerName, date, startTime, endTime);
        return "userSchedAppt";
    }

    @PostMapping("/userBookSlot/{id}")
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
        if (!"User".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        long userId = (long) session.getAttribute("userId");

        try {
            appointmentService.bookSlot(id, userId);
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            addSearchResults(model, category, providerName, date, startTime, endTime);
            return "userSchedAppt";
        }

        UriComponentsBuilder redirect = UriComponentsBuilder.fromPath("/userSchedAppt")
                .queryParam("booked", "1");
        addQueryParam(redirect, "category", category);
        addQueryParam(redirect, "providerName", providerName);
        addQueryParam(redirect, "date", date);
        addQueryParam(redirect, "startTime", startTime);
        addQueryParam(redirect, "endTime", endTime);
        return "redirect:" + redirect.toUriString();
    }

    private void addSearchResults(Model model, String category, String providerName,
                                  LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Appointment> slots = appointmentService.searchOpenSlots(
                category, providerName, date, startTime, endTime);
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

    @GetMapping("/userApptHistory")
    public String userApptHistory(HttpSession session) {
        if (!"User".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        return "userApptHistory";
    }

}