package hotel.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * הזמנת חדר ללקוח, מתאריך כניסה למספר לילות.
 */
public class Order implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int orderNumber;
    private final Guest guest;
    private final Room room;
    private final LocalDate checkIn;
    private final int nights;

    public Order(int orderNumber, Guest guest, Room room, LocalDate checkIn, int nights) {
        if (nights < 1) {
            throw new IllegalArgumentException("מספר הלילות חייב להיות לפחות 1");
        }
        this.orderNumber = orderNumber;
        this.guest = Objects.requireNonNull(guest, "לקוח הוא שדה חובה");
        this.room = Objects.requireNonNull(room, "חדר הוא שדה חובה");
        this.checkIn = Objects.requireNonNull(checkIn, "תאריך כניסה הוא שדה חובה");
        this.nights = nights;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public int getNights() {
        return nights;
    }

    /** תאריך העזיבה – היום שאחרי הלילה האחרון. */
    public LocalDate getCheckOut() {
        return checkIn.plusDays(nights);
    }

    /** האם ההזמנה חופפת לטווח [from, to), כלומר to הוא תאריך העזיבה. */
    public boolean overlaps(LocalDate from, LocalDate to) {
        return checkIn.isBefore(to) && from.isBefore(getCheckOut());
    }

    /** האם החדר תפוס בלילה של התאריך הנתון. */
    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(checkIn) && date.isBefore(getCheckOut());
    }

    public double calcPrice() {
        double basePrice = room.getLevel().getNightlyPrice() * nights;
        return basePrice * (1 - guest.getDiscountPercent() / 100.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return orderNumber == other.orderNumber;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(orderNumber);
    }

    @Override
    public String toString() {
        return String.format("הזמנה %d | %s | חדר %d | %s עד %s (%d לילות) | מחיר: %.2f ₪",
                orderNumber, guest.getFullName(), room.getNumber(), checkIn, getCheckOut(), nights, calcPrice());
    }
}
