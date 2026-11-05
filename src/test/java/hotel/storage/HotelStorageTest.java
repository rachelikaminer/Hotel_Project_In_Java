package hotel.storage;

import hotel.manager.Hotel;
import hotel.model.BusinessGuest;
import hotel.model.Room;
import hotel.model.RoomLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HotelStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadKeepAllData() throws IOException {
        Hotel hotel = new Hotel("מלון בדיקה", "כתובת", "02-0000000");
        hotel.addRoom(new Room(301, 3, RoomLevel.SUITE));
        hotel.registerGuest(new BusinessGuest("000000042", "משה", "גולדברג", null, null, null, "B-1"));
        hotel.bookRoom("000000042", 301, LocalDate.of(2026, 6, 1), 2);

        Path file = tempDir.resolve("hotel.dat");
        HotelStorage.save(hotel, file);
        Hotel loaded = HotelStorage.load(file);

        assertEquals("מלון בדיקה", loaded.getName());
        assertEquals(1, loaded.getOrderManager().getOrders().size());
        assertInstanceOf(BusinessGuest.class, loaded.getGuestManager().findById("000000042").orElseThrow());
        // מונה ההזמנות נשמר, כך שההזמנה הבאה מקבלת מספר חדש
        assertEquals(2, loaded.bookRoom("000000042", 301, LocalDate.of(2026, 7, 1), 1).getOrderNumber());
    }

    @Test
    void loadRejectsInvalidFile() throws IOException {
        Path file = tempDir.resolve("broken.dat");
        Files.writeString(file, "not a hotel");
        assertThrows(IOException.class, () -> HotelStorage.load(file));
    }

    @Test
    void exportReportWritesTitleAndItems() throws IOException {
        Path file = tempDir.resolve("report.txt");
        HotelStorage.exportReport("דוח", List.of("שורה א", "שורה ב"), file);
        assertEquals(List.of("דוח", "---", "שורה א", "שורה ב"), Files.readAllLines(file, StandardCharsets.UTF_8));
    }
}
