package hotel.model;

import java.io.Serial;

/**
 * לקוח של המלון.
 */
public class Guest extends Person {
    @Serial
    private static final long serialVersionUID = 1L;

    private int numVisits;

    public Guest(String id, String firstName, String lastName, String address, String phone, String email) {
        super(id, firstName, lastName, address, phone, email);
    }

    public int getNumVisits() {
        return numVisits;
    }

    public void incrementVisits() {
        numVisits++;
    }

    public void setNumVisits(int numVisits) {
        if (numVisits < 0) {
            throw new IllegalArgumentException("מספר ביקורים לא יכול להיות שלילי");
        }
        this.numVisits = numVisits;
    }
    /** אחוז ההנחה שהלקוח מקבל. ללקוח רגיל אין הנחה. */
    public double getDiscountPercent() {
        return 0;
    }

    @Override
    public String toString() {
        return super.toString() + " | ביקורים: " + numVisits;
    }
}
