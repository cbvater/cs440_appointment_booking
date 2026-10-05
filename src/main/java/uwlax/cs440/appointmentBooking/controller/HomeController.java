package uwlax.cs440.appointmentBooking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "login"; // matches templates/home.html (no .html needed)
    }
}