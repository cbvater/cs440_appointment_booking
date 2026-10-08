package uwlax.cs440.appointmentBooking.model;

import jakarta.persistence.*;

@Entity
public class ServiceProvider {
    @Id @GeneratedValue
    private long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String qualification;
    private String company;
    private String serviceType;
    public ServiceProvider() {}
    public void setId(long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getCompany() {
        return company;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getQualification() {
        return qualification;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }
}
