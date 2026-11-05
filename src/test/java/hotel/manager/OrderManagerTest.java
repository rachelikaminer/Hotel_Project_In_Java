package hotel.manager;

import hotel.model.Guest;
import hotel.model.Room;
import hotel.model.RoomLevel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderManagerTest {
    private static final LocalDate DAY = LocalDate.of(2026, 8, 1);

    private final OrderManager manager = new OrderManager();
    private final Guest guest = new Guest("000000018", "אברהם", "כהן", null, null, null);
    private final Room room = new Room(101, 1, RoomLevel.STANDARD);

    @Test
    void findsOrdersByNightsAndCheckIn() {
        manager.createOrder(guest, room, DAY, 4);
        manager.createOrder(guest, room, DAY.plusDays(10), 2);
        assertEquals(1, manager.findByNights(4).size());
        assertEquals(1, manager.findByCheckIn(DAY.plusDays(10)).size());
        assertEquals(2, manager.findByGuest(guest).size());
    }

    @Test
    void roomAvailabilityRespectsDateRange() {
        manager.createOrder(guest, room, DAY, 2);
        assertFalse(manager.isRoomAvailable(room, DAY.plusDays(1), DAY.plusDays(3)));
        assertTrue(manager.isRoomAvailable(room, DAY.plusDays(2), DAY.plusDays(4)));
    }
}
