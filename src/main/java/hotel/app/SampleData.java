package hotel.app;

import hotel.manager.Hotel;
import hotel.model.BusinessGuest;
import hotel.model.Guest;
import hotel.model.Room;
import hotel.model.RoomLevel;

import java.time.LocalDate;

/**
 * נתוני דמה להרצה ראשונה. כל הפרטים בדויים.
 */
public final class SampleData {

    private SampleData() {
    }

    public static Hotel createHotel() {
        Hotel hotel = new Hotel("מלון שלום", "רחוב הדוגמה 1, ירושלים", "02-0000000");

        hotel.addRoom(new Room(101, 1, RoomLevel.STANDARD));
        hotel.addRoom(new Room(102, 1, RoomLevel.STANDARD));
        hotel.addRoom(new Room(201, 2, RoomLevel.DELUXE));
        hotel.addRoom(new Room(202, 2, RoomLevel.DELUXE));
        hotel.addRoom(new Room(301, 3, RoomLevel.SUITE));

        hotel.registerGuest(new Guest("000000018", "אברהם", "כהן", "בני ברק", "050-0000001", "avraham@example.com"));
        hotel.registerGuest(new Guest("000000026", "שרה", "לוי", "ירושלים", "050-0000002", "sarah@example.com"));
        hotel.registerGuest(new Guest("000000034", "יצחק", "פרידמן", "אלעד", "050-0000003", "yitzchak@example.com"));
        hotel.registerGuest(new BusinessGuest("000000042", "משה", "גולדברג", "בית שמש", "050-0000004",
                "moshe@example.com", "B-5555"));

        LocalDate today = LocalDate.now();
        hotel.bookRoom("000000018", 101, today, 3);
        hotel.bookRoom("000000026", 201, today.plusDays(1), 2);
        hotel.bookRoom("000000042", 301, today, 4);
        hotel.bookRoom("000000034", 102, today.plusDays(7), 5);

        return hotel;
    }
}
