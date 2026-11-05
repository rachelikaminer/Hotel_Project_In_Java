package hotel.storage;

import hotel.manager.Hotel;
import hotel.model.BusinessGuest;
import hotel.model.Guest;
import hotel.model.Room;
import hotel.model.RoomLevel;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseStorageTest {

    // מסד נתונים בזיכרון בלבד, שנמחק בסוף הבדיקות
    private final DatabaseStorage storage =
            new DatabaseStorage("jdbc:h2:mem:test" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");

    @Test
    void emptyDatabaseHasNoData() throws SQLException {
        assertFalse(storage.hasData());
    }

    @Test
    void saveAndLoadKeepAllData() throws SQLException {
        Hotel hotel = new Hotel("מלון בדיקה", "כתובת", "02-0000000");
        hotel.addRoom(new Room(101, 1, RoomLevel.STANDARD));
        hotel.addRoom(new Room(301, 3, RoomLevel.SUITE));
        hotel.registerGuest(new Guest("000000018", "אברהם", "כהן", null, null, null));
        BusinessGuest businessGuest = new BusinessGuest("000000042", "משה", "גולדברג", null, null, null, "B-1");
        businessGuest.setDiscountPercent(15);
        hotel.registerGuest(businessGuest);
        hotel.bookRoom("000000018", 101, LocalDate.of(2026, 6, 1), 2);
        int cancelled = hotel.bookRoom("000000042", 301, LocalDate.of(2026, 6, 1), 3).getOrderNumber();
        hotel.cancelOrder(cancelled);

        storage.save(hotel);
        assertTrue(storage.hasData());
        Hotel loaded = storage.load();

        assertEquals("מלון בדיקה", loaded.getName());
        assertEquals(2, loaded.getRoomManager().getRooms().size());
        assertEquals(1, loaded.getOrderManager().getOrders().size());
        Guest loadedBusiness = loaded.getGuestManager().findById("000000042").orElseThrow();
        assertInstanceOf(BusinessGuest.class, loadedBusiness);
        assertEquals(15, loadedBusiness.getDiscountPercent());
        assertEquals(1, loadedBusiness.getNumVisits());
        // ההזמנה שבוטלה לא מקבלת שוב את אותו מספר
        assertEquals(3, loaded.bookRoom("000000018", 301, LocalDate.of(2026, 7, 1), 1).getOrderNumber());
    }

    @Test
    void savingTwiceReplacesPreviousData() throws SQLException {
        Hotel hotel = new Hotel("מלון", null, null);
        hotel.addRoom(new Room(101, 1, RoomLevel.STANDARD));
        storage.save(hotel);
        hotel.addRoom(new Room(102, 1, RoomLevel.STANDARD));
        storage.save(hotel);
        assertEquals(2, storage.load().getRoomManager().getRooms().size());
    }
}