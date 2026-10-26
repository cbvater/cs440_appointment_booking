package uwlax.cs440.appointmentBooking.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import uwlax.cs440.appointmentBooking.service.NotificationService;

@ControllerAdvice
public class NotificationBadge {
    private final NotificationService notificationService;

    private NotificationBadge(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // this is for the number of notifications are unchecked
    @ModelAttribute("unreadCount")
    public long unreadCount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session == null || session.getAttribute("userId") == null) {
            return 0;
        }
        return notificationService.countUnchecked((long) session.getAttribute("userId"));
    }
}
