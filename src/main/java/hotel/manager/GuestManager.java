package hotel.manager;

import hotel.model.Guest;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * ניהול לקוחות המלון.
 */
public class GuestManager implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final List<Guest> guests = new ArrayList<>();

    /**
     * מוסיף לקוח חדש.
     *
     * @return false אם כבר קיים לקוח עם אותה ת.ז.
     */
    public boolean addGuest(Guest guest) {
        Objects.requireNonNull(guest, "לקוח הוא שדה חובה");
        if (findById(guest.getId()).isPresent()) {
            return false;
        }
        guests.add(guest);
        return true;
    }

    public Optional<Guest> findById(String id) {
        return guests.stream().filter(g -> g.getId().equals(id)).findFirst();
    }

    public List<Guest> findByName(String name) {
        return search(g -> g.getFirstName().equalsIgnoreCase(name) || g.getFullName().equalsIgnoreCase(name));
    }

    public List<Guest> search(Predicate<Guest> predicate) {
        return guests.stream().filter(predicate).toList();
    }

    public boolean removeGuest(String id) {
        return guests.removeIf(g -> g.getId().equals(id));
    }

    public List<Guest> getGuests() {
        return Collections.unmodifiableList(guests);
    }
}
