package hotel.model;

/**
 * דרגת חדר, וקובעת את המחיר ללילה.
 */
public enum RoomLevel {
    STANDARD("רגיל", 350),
    DELUXE("דלוקס", 550),
    SUITE("סוויטה", 900);

    private final String displayName;
    private final double nightlyPrice;

    RoomLevel(String displayName, double nightlyPrice) {
        this.displayName = displayName;
        this.nightlyPrice = nightlyPrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getNightlyPrice() {
        return nightlyPrice;
    }
}
