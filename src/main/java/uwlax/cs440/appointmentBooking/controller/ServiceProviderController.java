package uwlax.cs440.appointmentBooking.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uwlax.cs440.appointmentBooking.model.ServiceProvider;
import uwlax.cs440.appointmentBooking.service.ServiceProviderService;

@Controller
public class ServiceProviderController {

    private final ServiceProviderService serviceProviderService;
    public ServiceProviderController(ServiceProviderService serviceProviderService){
        this.serviceProviderService = serviceProviderService;
    }


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
            ServiceProvider provider = serviceProviderService.register(firstName, lastName, password, username, qualification, company, serviceType);

            session.setAttribute("user", provider.getUser());
        } catch (IllegalArgumentException e){
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
    public String showRegistrationForm(){
        return "serviceProviderRegistration";
    }
}
