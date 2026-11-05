package hotel.manager;

import hotel.model.Guest;
import hotel.model.Order;
import hotel.model.Room;
import hotel.model.RoomLevel;
import hotel.model.RoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotelTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 5, 1);
    private static final String GUEST_ID = "000000018";

    private Hotel hotel;
    private Room room101;

    @BeforeEach
    void setUp() {
        hotel = new Hotel("מלון בדיקה", "כתובת", "02-0000000");
        room101 = new Room(101, 1, RoomLevel.STANDARD);
        hotel.addRoom(room101);
        hotel.addRoom(new Room(102, 1, RoomLevel.SUITE));
        hotel.registerGuest(new Guest(GUEST_ID, "אברהם", "כהן", null, null, null));
    }

    @Test
    void registersNewGuestAndRejectsDuplicateId() {
        assertTrue(hotel.registerGuest(new Guest("000000026", "שרה", "לוי", null, null, null)));
        assertFalse(hotel.registerGuest(new Guest(GUEST_ID, "אחר", "אחר", null, null, null)));
        assertEquals(2, hotel.getGuestManager().getGuests().size());
    }

    @Test
    void rejectsDuplicateRoomNumber() {
        assertThrows(IllegalArgumentException.class, () -> hotel.addRoom(new Room(101, 3, RoomLevel.DELUXE)));
    }

    @Test
    void bookingMarksRoomOccupiedOnlyDuringStay() {
        hotel.bookRoom(GUEST_ID, 101, TODAY, 2);
        assertEquals(RoomStatus.OCCUPIED, hotel.getRoomStatus(room101, TODAY));
        assertEquals(RoomStatus.OCCUPIED, hotel.getRoomStatus(room101, TODAY.plusDays(1)));
        assertEquals(RoomStatus.AVAILABLE, hotel.getRoomStatus(room101, TODAY.plusDays(2)));
    }

    @Test
    void bookingIncrementsGuestVisits() {
        hotel.bookRoom(GUEST_ID, 101, TODAY, 1);
        hotel.bookRoom(GUEST_ID, 101, TODAY.plusDays(5), 1);
        assertEquals(2, hotel.getGuestManager().findById(GUEST_ID).orElseThrow().getNumVisits());
    }

    @Test
    void rejectsOverlappingBooking() {
        hotel.bookRoom(GUEST_ID, 101, TODAY, 3);
        assertThrows(IllegalStateException.class, () -> hotel.bookRoom(GUEST_ID, 101, TODAY.plusDays(2), 2));
    }

    @Test
    void allowsBookingStartingOnPreviousCheckOut() {
        hotel.bookRoom(GUEST_ID, 101, TODAY, 3);
        Order next = hotel.bookRoom(GUEST_ID, 101, TODAY.plusDays(3), 1);
        assertEquals(TODAY.plusDays(3), next.getCheckIn());
    }

    @Test
    void rejectsUnknownGuestOrRoom() {
        assertThrows(IllegalArgumentException.class, () -> hotel.bookRoom("999999999", 101, TODAY, 1));
        assertThrows(IllegalArgumentException.class, () -> hotel.bookRoom(GUEST_ID, 999, TODAY, 1));
    }

    @Test
    void cancellingOrderFreesRoom() {
        Order order = hotel.bookRoom(GUEST_ID, 101, TODAY, 2);
        assertTrue(hotel.cancelOrder(order.getOrderNumber()));
        assertEquals(RoomStatus.AVAILABLE, hotel.getRoomStatus(room101, TODAY));
    }

    @Test
    void cancellingUnknownOrderReturnsFalse() {
        assertFalse(hotel.cancelOrder(42));
    }

    @Test
    void orderNumbersAreNotReusedAfterCancel() {
        Order first = hotel.bookRoom(GUEST_ID, 101, TODAY, 1);
        Order second = hotel.bookRoom(GUEST_ID, 102, TODAY, 1);
        hotel.cancelOrder(first.getOrderNumber());
        Order third = hotel.bookRoom(GUEST_ID, 101, TODAY, 1);
        assertNotEquals(second.getOrderNumber(), third.getOrderNumber());
        assertNotEquals(first.getOrderNumber(), third.getOrderNumber());
    }

    @Test
    void findAvailableRoomsExcludesBookedRooms() {
        hotel.bookRoom(GUEST_ID, 101, TODAY, 2);
        List<Room> available = hotel.findAvailableRooms(TODAY.plusDays(1), 1);
        assertEquals(List.of(102), available.stream().map(Room::getNumber).toList());
    }

    @Test
    void cannotRemoveGuestWithUpcomingOrder() {
        hotel.bookRoom(GUEST_ID, 101, TODAY.plusDays(3), 1);
        assertThrows(IllegalStateException.class, () -> hotel.removeGuest(GUEST_ID, TODAY));
    }

    @Test
    void canRemoveGuestWhosePastOrdersEnded() {
        hotel.bookRoom(GUEST_ID, 101, TODAY.minusDays(5), 2);
        assertTrue(hotel.removeGuest(GUEST_ID, TODAY));
        assertTrue(hotel.getGuestManager().findById(GUEST_ID).isEmpty());
    }

    @Test
    void activeGuestsAreThoseWithCurrentOrFutureOrders() {
        hotel.registerGuest(new Guest("000000026", "שרה", "לוי", null, null, null));
        hotel.bookRoom(GUEST_ID, 101, TODAY, 1);
        hotel.bookRoom("000000026", 102, TODAY.minusDays(3), 1);
        List<String> activeIds = hotel.getActiveGuests(TODAY).stream().map(Guest::getId).toList();
        assertEquals(List.of(GUEST_ID), activeIds);
    }
}
