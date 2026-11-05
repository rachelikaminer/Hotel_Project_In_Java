package hotel.manager;

import hotel.model.Room;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * ניהול חדרי המלון.
 */
public class RoomManager implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final List<Room> rooms = new ArrayList<>();

    public void addRoom(Room room) {
        Objects.requireNonNull(room, "חדר הוא שדה חובה");
        if (findByNumber(room.getNumber()).isPresent()) {
            throw new IllegalArgumentException("חדר " + room.getNumber() + " כבר קיים");
        }
        rooms.add(room);
    }

    public Optional<Room> findByNumber(int number) {
        return rooms.stream().filter(r -> r.getNumber() == number).findFirst();
    }

    public List<Room> search(Predicate<Room> predicate) {
        return rooms.stream().filter(predicate).toList();
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }
}
