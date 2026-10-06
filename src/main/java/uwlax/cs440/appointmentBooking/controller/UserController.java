package uwlax.cs440.appointmentBooking.controller;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uwlax.cs440.appointmentBooking.model.User;
import uwlax.cs440.appointmentBooking.service.UserService;

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
    public UserController(UserService userService){
        this.userService = userService;
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


    @GetMapping("/serviceProviderHome")
    public String showProviderHome(HttpSession session) {
        if (!"Service Provider".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        return "serviceProviderHome";
    }

    @GetMapping("/adminHome")
    public String showAdminHome(HttpSession session) {
        if (!"Admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        return "adminHome";
    }
}