package uwlax.cs440.appointmentBooking.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uwlax.cs440.appointmentBooking.model.ServiceProvider;
import uwlax.cs440.appointmentBooking.model.User;
import uwlax.cs440.appointmentBooking.repository.ServiceProviderRepository;
import uwlax.cs440.appointmentBooking.repository.UserRepository;

import java.util.Optional;

@Service
public class ServiceProviderService {
    private final ServiceProviderRepository serviceProviderRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.serviceProviderRepository = serviceProviderRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public ServiceProvider register(String firstName, String lastName, String password, String username, String qualification, String company, String serviceType) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if(userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(true);
        user.setRole("Service Provider");
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);

        ServiceProvider provider = new ServiceProvider();
        provider.setUser(user);
        provider.setCompany(company);
        provider.setQualification(qualification);
        provider.setServiceType(serviceType);
        return serviceProviderRepository.save(provider);
    }
}
