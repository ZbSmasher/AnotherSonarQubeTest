package tqs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MealsBookingServiceTest {

    @Test @DisplayName("Book returns a token; details are retrievable and ACTIVE")
    void bookAndGet() {
        MealsBookingService svc = new MealsBookingService(10);
        String token = svc.book("s123", LocalDate.of(2025, 9, 25), MealShift.LUNCH);
        Booking b = svc.get(token);
        assertEquals("s123", b.getStudentId());
        assertEquals(LocalDate.of(2025, 9, 25), b.getDate());
        assertEquals(MealShift.LUNCH, b.getShift());
        assertEquals(BookingStatus.ACTIVE, b.getStatus());
    }

    @Test @DisplayName("No double booking for the same student+date+shift")
    void noDoubleBookingSameShift() {
        MealsBookingService svc = new MealsBookingService(5);
        LocalDate d = LocalDate.of(2025, 9, 25);
        svc.book("s1", d, MealShift.LUNCH);
        assertThrows(IllegalStateException.class, () -> svc.book("s1", d, MealShift.LUNCH));
    }

    @Test @DisplayName("Same day but different shift is allowed")
    void differentShiftAllowed() {
        MealsBookingService svc = new MealsBookingService(5);
        LocalDate d = LocalDate.of(2025, 9, 25);
        svc.book("s1", d, MealShift.LUNCH);
        String t2 = svc.book("s1", d, MealShift.DINNER);
        assertNotNull(t2);
    }

    @Test @DisplayName("Capacity per (date,shift) is enforced")
    void capacityEnforced() {
        MealsBookingService svc = new MealsBookingService(2);
        LocalDate d = LocalDate.of(2025, 9, 25);
        svc.book("a", d, MealShift.LUNCH);
        svc.book("b", d, MealShift.LUNCH);
        assertThrows(IllegalStateException.class, () -> svc.book("c", d, MealShift.LUNCH));
        // But dinner is still available
        String token = svc.book("c", d, MealShift.DINNER);
        assertNotNull(token);
    }

    @Test @DisplayName("Check-in marks USED; used token cannot be reused")
    void checkInOnceOnly() {
        MealsBookingService svc = new MealsBookingService(5);
        LocalDate d = LocalDate.of(2025, 9, 25);
        String token = svc.book("s1", d, MealShift.LUNCH);
        svc.checkIn(token);
        assertEquals(BookingStatus.USED, svc.get(token).getStatus());
        assertThrows(IllegalStateException.class, () -> svc.checkIn(token));
    }

    @Test @DisplayName("Canceled tokens cannot be used")
    void cancelPreventsUse() {
        MealsBookingService svc = new MealsBookingService(5);
        LocalDate d = LocalDate.of(2025, 9, 25);
        String token = svc.book("s1", d, MealShift.LUNCH);
        svc.cancel(token);
        assertEquals(BookingStatus.CANCELED, svc.get(token).getStatus());
        assertThrows(IllegalStateException.class, () -> svc.checkIn(token));
    }

    @Test @DisplayName("After cancel, the student can book the same (date,shift) again if capacity allows")
    void cancelFreesSlot() {
        MealsBookingService svc = new MealsBookingService(1);
        LocalDate d = LocalDate.of(2025, 9, 25);
        String t1 = svc.book("s1", d, MealShift.LUNCH);
        svc.cancel(t1);
        String t2 = svc.book("s1", d, MealShift.LUNCH);
        assertNotNull(t2);
    }

    @Test @DisplayName("Unknown tokens cause IllegalArgumentException on get/cancel/checkIn")
    void invalidTokenGuards() {
        MealsBookingService svc = new MealsBookingService(5);
        assertThrows(IllegalArgumentException.class, () -> svc.get("nope"));
        assertThrows(IllegalArgumentException.class, () -> svc.cancel("nope"));
        assertThrows(IllegalArgumentException.class, () -> svc.checkIn("nope"));
    }
}
