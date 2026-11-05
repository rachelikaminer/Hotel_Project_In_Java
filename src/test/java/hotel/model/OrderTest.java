package hotel.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {
    private static final LocalDate CHECK_IN = LocalDate.of(2026, 3, 10);

    private final Room deluxeRoom = new Room(201, 2, RoomLevel.DELUXE);
    private final Guest guest = new Guest("000000018", "אברהם", "כהן", null, null, null);

    @Test
    void priceIsNightlyRateTimesNights() {
        Order order = new Order(1, guest, deluxeRoom, CHECK_IN, 3);
        assertEquals(RoomLevel.DELUXE.getNightlyPrice() * 3, order.calcPrice(), 0.001);
    }

    @Test
    void businessGuestGetsDiscount() {
        BusinessGuest businessGuest = new BusinessGuest("000000042", "משה", "גולדברג", null, null, null, "B-1");
        businessGuest.setDiscountPercent(20);
        Order order = new Order(1, businessGuest, deluxeRoom, CHECK_IN, 2);
        assertEquals(RoomLevel.DELUXE.getNightlyPrice() * 2 * 0.8, order.calcPrice(), 0.001);
    }

    @Test
    void checkOutIsDayAfterLastNight() {
        Order order = new Order(1, guest, deluxeRoom, CHECK_IN, 3);
        assertEquals(LocalDate.of(2026, 3, 13), order.getCheckOut());
    }

    @Test
    void overlapTreatsCheckOutDayAsFree() {
        Order order = new Order(1, guest, deluxeRoom, CHECK_IN, 3);
        assertTrue(order.overlaps(CHECK_IN.plusDays(2), CHECK_IN.plusDays(5)));
        assertFalse(order.overlaps(order.getCheckOut(), order.getCheckOut().plusDays(2)));
        assertFalse(order.overlaps(CHECK_IN.minusDays(2), CHECK_IN));
    }

    @Test
    void rejectsZeroNights() {
        assertThrows(IllegalArgumentException.class, () -> new Order(1, guest, deluxeRoom, CHECK_IN, 0));
    }

    @Test
    void businessGuestStoresNameAndIdInCorrectFields() {
        BusinessGuest businessGuest = new BusinessGuest("000000042", "משה", "גולדברג", null, null, null, "B-1");
        assertEquals("000000042", businessGuest.getId());
        assertEquals("משה", businessGuest.getFirstName());
        assertEquals("גולדברג", businessGuest.getLastName());
    }

    @Test
    void personEqualityIsById() {
        Guest sameGuest = new Guest("000000018", "אברהם", "כהן", null, null, null);
        sameGuest.incrementVisits();
        sameGuest.setPhone("050-0000009");
        assertEquals(guest, sameGuest);
    }
}
