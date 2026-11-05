package hotel.app;

import hotel.manager.Hotel;
import hotel.model.BusinessGuest;
import hotel.model.Guest;
import hotel.model.Order;
import hotel.model.Room;
import hotel.model.RoomStatus;
import hotel.storage.HotelStorage;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * תפריט טקסטואלי לניהול המלון.
 */
public class ConsoleMenu {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int EXIT = 0;

    private final Hotel hotel;
    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleMenu(Hotel hotel, Scanner scanner, PrintStream out) {
        this.hotel = hotel;
        this.scanner = scanner;
        this.out = out;
    }

    public void run() {
        while (true) {
            printMenu();
            int choice = readInt("בחירה: ");
            if (choice == EXIT) {
                return;
            }
            try {
                handle(choice);
            } catch (IllegalArgumentException | IllegalStateException e) {
                out.println("שגיאה: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        out.println();
        out.println("=== תפריט בית המלון ===");
        out.println("1. פרטי בית המלון");
        out.println("2. הזמנת חדר");
        out.println("3. ביטול הזמנה");
        out.println("4. הצגת כל ההזמנות");
        out.println("5. דוח חדרים פנויים ותפוסים");
        out.println("6. חיפוש חדרים פנויים לתאריכים");
        out.println("7. הצגת לקוחות");
        out.println("8. הוספת לקוח");
        out.println("9. מחיקת לקוח");
        out.println("10. ייצוא דוח הזמנות לקובץ");
        out.println("0. שמירה ויציאה");
    }

    private void handle(int choice) {
        switch (choice) {
            case 1 -> out.println(hotel);
            case 2 -> bookRoom();
            case 3 -> cancelOrder();
            case 4 -> printList("הזמנות", hotel.getOrderManager().getOrders());
            case 5 -> printRoomStatusReport();
            case 6 -> searchAvailableRooms();
            case 7 -> printGuests();
            case 8 -> addGuest();
            case 9 -> removeGuest();
            case 10 -> exportOrders();
            default -> out.println("אפשרות לא קיימת, נסו שוב.");
        }
    }

    private void bookRoom() {
        String guestId = readLine("ת.ז. של הלקוח: ");
        LocalDate checkIn = readDate("תאריך כניסה");
        int nights = readInt("מספר לילות: ");
        List<Room> available = hotel.findAvailableRooms(checkIn, nights);
        if (available.isEmpty()) {
            out.println("אין חדרים פנויים בתאריכים האלה.");
            return;
        }
        printList("חדרים פנויים", available);
        int roomNumber = readInt("מספר חדר: ");
        Order order = hotel.bookRoom(guestId, roomNumber, checkIn, nights);
        out.println("ההזמנה נקלטה: " + order);
    }

    private void cancelOrder() {
        int orderNumber = readInt("מספר הזמנה לביטול: ");
        out.println(hotel.cancelOrder(orderNumber) ? "ההזמנה בוטלה." : "לא נמצאה הזמנה מספר " + orderNumber);
    }

    private void printRoomStatusReport() {
        LocalDate date = readDate("תאריך לדוח");
        out.printf("%-8s %-6s %-10s %-6s%n", "חדר", "קומה", "דרגה", "מצב");
        for (Room room : hotel.getRoomManager().getRooms()) {
            out.printf("%-8d %-6d %-10s %-6s%n", room.getNumber(), room.getFloor(),
                    room.getLevel().getDisplayName(), hotel.getRoomStatus(room, date).getDisplayName());
        }
        out.printf("סה\"כ פנויים: %d, תפוסים: %d%n",
                hotel.getRoomsByStatus(RoomStatus.AVAILABLE, date).size(),
                hotel.getRoomsByStatus(RoomStatus.OCCUPIED, date).size());
    }

    private void searchAvailableRooms() {
        LocalDate checkIn = readDate("תאריך כניסה");
        int nights = readInt("מספר לילות: ");
        printList("חדרים פנויים", hotel.findAvailableRooms(checkIn, nights));
    }

    private void printGuests() {
        List<Guest> guests = hotel.getGuestManager().getGuests();
        List<Guest> activeGuests = hotel.getActiveGuests(LocalDate.now());
        out.printf("%-12s %-20s %-14s %-6s%n", "ת.ז.", "שם", "טלפון", "פעיל");
        for (Guest guest : guests) {
            out.printf("%-12s %-20s %-14s %-6s%n", guest.getId(), guest.getFullName(), guest.getPhone(),
                    activeGuests.contains(guest) ? "כן" : "לא");
        }
    }

    private void addGuest() {
        String id = readLine("ת.ז.: ");
        String firstName = readLine("שם פרטי: ");
        String lastName = readLine("שם משפחה: ");
        String address = readLine("כתובת: ");
        String phone = readLine("טלפון: ");
        String email = readLine("אימייל: ");
        String businessCode = readLine("קוד לקוח עסקי (Enter ללקוח רגיל): ");
        Guest guest = businessCode.isBlank()
                ? new Guest(id, firstName, lastName, address, phone, email)
                : new BusinessGuest(id, firstName, lastName, address, phone, email, businessCode);
        out.println(hotel.registerGuest(guest) ? "הלקוח נוסף." : "כבר קיים לקוח עם ת.ז. " + id);
    }

    private void removeGuest() {
        String id = readLine("ת.ז. של הלקוח למחיקה: ");
        out.println(hotel.removeGuest(id, LocalDate.now()) ? "הלקוח נמחק." : "לא נמצא לקוח עם ת.ז. " + id);
    }

    private void exportOrders() {
        Path file = Path.of("orders-report.txt");
        try {
            HotelStorage.exportReport("דוח הזמנות – " + hotel.getName(), hotel.getOrderManager().getOrders(), file);
            out.println("הדוח נשמר בקובץ " + file.toAbsolutePath());
        } catch (IOException e) {
            out.println("שגיאה בכתיבת הדוח: " + e.getMessage());
        }
    }

    private void printList(String title, List<?> items) {
        out.println("--- " + title + " (" + items.size() + ") ---");
        if (items.isEmpty()) {
            out.println("אין נתונים להצגה.");
        }
        items.forEach(out::println);
    }

    private String readLine(String prompt) {
        out.print(prompt);
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            // סוף הקלט (למשל Ctrl+D) – יוצאים בצורה מסודרת
            throw new InputClosedException();
        }
    }

    private int readInt(String prompt) {
        while (true) {
            String text = readLine(prompt);
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                out.println("יש להזין מספר שלם.");
            }
        }
    }

    private LocalDate readDate(String label) {
        while (true) {
            String text = readLine(label + " (dd/MM/yyyy, Enter להיום): ");
            if (text.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(text, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                out.println("תאריך לא תקין. דוגמה: 25/12/2026");
            }
        }
    }

    /** נזרקת כשהקלט נסגר, כדי לצאת מהתפריט ולשמור את הנתונים. */
    public static class InputClosedException extends RuntimeException {
        public InputClosedException() {
            super("הקלט נסגר");
        }
    }
}
