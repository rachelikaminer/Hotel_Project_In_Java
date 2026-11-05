package hotel.app;

import hotel.manager.Hotel;
import hotel.storage.DatabaseStorage;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    // הקובץ hotel-db.mv.db ייווצר בתיקיית הפרויקט
    private static final String DB_URL = "jdbc:h2:./hotel-db";

    public static void main(String[] args) {
        DatabaseStorage storage = new DatabaseStorage(DB_URL);
        Hotel hotel = loadOrCreate(storage);
        try {
            new ConsoleMenu(hotel, new Scanner(System.in), System.out).run();
        } catch (ConsoleMenu.InputClosedException e) {
            System.out.println();
        }
        save(storage, hotel);
    }

    private static Hotel loadOrCreate(DatabaseStorage storage) {
        try {
            if (storage.hasData()) {
                System.out.println("הנתונים נטענו ממסד הנתונים.");
                return storage.load();
            }
        } catch (SQLException e) {
            System.out.println("לא ניתן לטעון את הנתונים (" + e.getMessage() + "), נטענים נתוני דמה.");
        }
        return SampleData.createHotel();
    }

    private static void save(DatabaseStorage storage, Hotel hotel) {
        try {
            storage.save(hotel);
            System.out.println("הנתונים נשמרו. להתראות!");
        } catch (SQLException e) {
            System.out.println("שגיאה בשמירת הנתונים: " + e.getMessage());
        }
    }
}