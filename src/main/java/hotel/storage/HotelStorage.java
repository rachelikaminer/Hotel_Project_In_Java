package hotel.storage;

import hotel.manager.Hotel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * שמירה וטעינה של נתוני המלון, וייצוא דוחות לקובץ טקסט.
 */
public final class HotelStorage {

    private HotelStorage() {
    }

    public static void save(Hotel hotel, Path file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(hotel);
        }
    }

    public static Hotel load(Path file) throws IOException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            return (Hotel) in.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("קובץ הנתונים אינו תקין: " + file, e);
        }
    }

    /** כותב דוח קריא: שורת כותרת ואחריה שורה לכל פריט. */
    public static void exportReport(String title, List<?> items, Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(title);
        lines.add("-".repeat(title.length()));
        items.forEach(item -> lines.add(item.toString()));
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
