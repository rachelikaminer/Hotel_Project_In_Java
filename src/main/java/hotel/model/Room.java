package hotel.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * חדר במלון. מזוהה לפי מספר החדר.
 * החדר עצמו לא שומר אם הוא תפוס – זה נקבע לפי ההזמנות לתאריך מסוים.
 */
public class Room implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int number;
    private int floor;
    private RoomLevel level;

    public Room(int number, int floor, RoomLevel level) {
        if (number <= 0) {
            throw new IllegalArgumentException("מספר חדר חייב להיות חיובי");
        }
        this.number = number;
        setFloor(floor);
        setLevel(level);
    }

    public int getNumber() {
        return number;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        if (floor < 0) {
            throw new IllegalArgumentException("קומה לא יכולה להיות שלילית");
        }
        this.floor = floor;
    }

    public RoomLevel getLevel() {
        return level;
    }

    public void setLevel(RoomLevel level) {
        this.level = Objects.requireNonNull(level, "דרגת חדר היא שדה חובה");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room other)) return false;
        return number == other.number;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(number);
    }

    @Override
    public String toString() {
        return String.format("חדר %d | קומה %d | %s | %.0f ₪ ללילה",
                number, floor, level.getDisplayName(), level.getNightlyPrice());
    }
}
