package hotel.model;

/**
 * מצב חדר בתאריך מסוים.
 */
public enum RoomStatus {
    AVAILABLE("פנוי"),
    OCCUPIED("תפוס");

    private final String displayName;

    RoomStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
