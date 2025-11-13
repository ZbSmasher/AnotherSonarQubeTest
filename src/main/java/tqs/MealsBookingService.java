package tqs;

import java.time.LocalDate;
import java.util.*;

public class MealsBookingService {
    private final int capacityPerShift;

    // token -> booking
    private final Map<String, Booking> bookings = new HashMap<>();
    // (student|date|shift) -> token (only ACTIVE)
    private final Map<String, String> activeByStudent = new HashMap<>();
    // (date|shift) -> ACTIVE tokens
    private final Map<String, Set<String>> activeTokensBySlot = new HashMap<>();

    public MealsBookingService(int capacityPerShift) {
        if (capacityPerShift <= 0) throw new IllegalArgumentException("capacityPerShift must be > 0");
        this.capacityPerShift = capacityPerShift;
    }

    public String book(String studentId, LocalDate date, MealShift shift) {
        Objects.requireNonNull(studentId, "studentId");
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(shift, "shift");

        String studentKey = studentKey(studentId, date, shift);
        if (activeByStudent.containsKey(studentKey)) {
            throw new IllegalStateException("Student already has an active booking for this slot.");
        }

        String slotKey = slotKey(date, shift);
        Set<String> slotTokens = activeTokensBySlot.computeIfAbsent(slotKey, k -> new HashSet<>());
        if (slotTokens.size() >= capacityPerShift) {
            throw new IllegalStateException("Capacity reached for slot " + slotKey);
        }

        String token = UUID.randomUUID().toString();
        Booking b = new Booking(token, studentId, date, shift);
        bookings.put(token, b);
        activeByStudent.put(studentKey, token);
        slotTokens.add(token);
        return token;
    }

    public Booking get(String token) {
        Booking b = bookings.get(token);
        if (b == null) throw new IllegalArgumentException("Unknown token");
        return b;
    }

    public void cancel(String token) {
        Booking b = requireKnown(token);
        if (b.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE bookings can be canceled");
        }
        b.setStatus(BookingStatus.CANCELED);
        removeFromActiveMaps(b);
    }

    public void checkIn(String token) {
        Booking b = requireKnown(token);
        if (b.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE bookings can be used to check-in");
        }
        b.setStatus(BookingStatus.USED);
        removeFromActiveMaps(b);
    }

    // ---- helpers ----
    private Booking requireKnown(String token) {
        Booking b = bookings.get(token);
        if (b == null) throw new IllegalArgumentException("Unknown token");
        return b;
    }

    private void removeFromActiveMaps(Booking b) {
        String studentKey = studentKey(b.getStudentId(), b.getDate(), b.getShift());
        activeByStudent.remove(studentKey);
        String slotKey = slotKey(b.getDate(), b.getShift());
        Set<String> set = activeTokensBySlot.get(slotKey);
        if (set != null) {
            set.remove(b.getToken());
            if (set.isEmpty()) activeTokensBySlot.remove(slotKey);
        }
    }

    private static String slotKey(LocalDate d, MealShift s) {
        return d + "|" + s.name();
    }
    private static String studentKey(String studentId, LocalDate d, MealShift s) {
        return studentId + "|" + slotKey(d, s);
    }
}
