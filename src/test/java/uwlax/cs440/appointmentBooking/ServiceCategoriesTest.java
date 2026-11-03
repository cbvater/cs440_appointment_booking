package uwlax.cs440.appointmentBooking;

import org.junit.jupiter.api.Test;
import uwlax.cs440.appointmentBooking.service.ServiceCategories;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceCategoriesTest {

    @Test
    void acceptsEveryListedCategory() {
        ServiceCategories.ALL.forEach(c -> assertThat(ServiceCategories.isValid(c)).isTrue());
    }

    @Test
    void rejectsFreeTextBlankAndNull() {
        assertThat(ServiceCategories.isValid("Plumbing")).isFalse();
        assertThat(ServiceCategories.isValid("medical")).isFalse();
        assertThat(ServiceCategories.isValid("")).isFalse();
        assertThat(ServiceCategories.isValid(null)).isFalse();
    }
}
