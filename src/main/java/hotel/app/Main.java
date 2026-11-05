package hotel.app;

import hotel.manager.Hotel;
import hotel.storage.HotelStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    private static final Path DATA_FILE = Path.of("hotel-data.dat");

    public static void main(String[] args) {
        Hotel hotel = loadOrCreate();
        try {
            new ConsoleMenu(hotel, new Scanner(System.in), System.out).run();
        } catch (ConsoleMenu.InputClosedException e) {
            System.out.println();
        }
        save(hotel);
    }

    private static Hotel loadOrCreate() {
        if (Files.exists(DATA_FILE)) {
            try {
                Hotel hotel = HotelStorage.load(DATA_FILE);
                System.out.println("הנתונים נטענו מהקובץ " + DATA_FILE);
                return hotel;
            } catch (IOException e) {
                System.out.println("לא ניתן לטעון את הנתונים (" + e.getMessage() + "), נטענים נתוני דמה.");
            }
        }
        return SampleData.createHotel();
    }

    private static void save(Hotel hotel) {
        try {
            HotelStorage.save(hotel, DATA_FILE);
            System.out.println("הנתונים נשמרו. להתראות!");
        } catch (IOException e) {
            System.out.println("שגיאה בשמירת הנתונים: " + e.getMessage());
        }
    }
}
