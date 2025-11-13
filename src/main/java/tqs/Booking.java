package tqs;

import java.time.LocalDate;

public class Booking {
    private final String token;
    private final String studentId;
    private final LocalDate date;
    private final MealShift shift;
    private BookingStatus status;

    public Booking(String token, String studentId, LocalDate date, MealShift shift) {
        this.token = token;
        this.studentId = studentId;
        this.date = date;
        this.shift = shift;
        this.status = BookingStatus.ACTIVE;
    }

    public String getToken()    { return token; }
    public String getStudentId(){ return studentId; }
    public LocalDate getDate()  { return date; }
    public MealShift getShift() { return shift; }
    public BookingStatus getStatus() { return status; }
    void setStatus(BookingStatus status) { this.status = status; }
}
