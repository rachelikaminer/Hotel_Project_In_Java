package hotel.manager;

import hotel.model.Guest;
import hotel.model.Order;
import hotel.model.Room;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * ניהול ההזמנות. מספרי ההזמנות רצים ואינם חוזרים על עצמם גם אחרי ביטול.
 */
public class OrderManager implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final List<Order> orders = new ArrayList<>();
    private int lastOrderNumber;

    public Order createOrder(Guest guest, Room room, LocalDate checkIn, int nights) {
        Order order = new Order(lastOrderNumber + 1, guest, room, checkIn, nights);
        lastOrderNumber++;
        orders.add(order);
        return order;
    }

    /** מחזיר הזמנה קיימת, למשל בטעינה ממסד הנתונים, בלי לתת לה מספר חדש. */
    public void restoreOrder(Order order) {
        if (findByNumber(order.getOrderNumber()).isPresent()) {
            throw new IllegalArgumentException("הזמנה " + order.getOrderNumber() + " כבר קיימת");
        }
        orders.add(order);
        lastOrderNumber = Math.max(lastOrderNumber, order.getOrderNumber());
    }

    public int getLastOrderNumber() {
        return lastOrderNumber;
    }

    public void restoreLastOrderNumber(int lastOrderNumber) {
        this.lastOrderNumber = Math.max(this.lastOrderNumber, lastOrderNumber);
    }
    public Optional<Order> findByNumber(int orderNumber) {
        return orders.stream().filter(o -> o.getOrderNumber() == orderNumber).findFirst();
    }

    public boolean removeOrder(int orderNumber) {
        return orders.removeIf(o -> o.getOrderNumber() == orderNumber);
    }

    public List<Order> search(Predicate<Order> predicate) {
        return orders.stream().filter(predicate).toList();
    }

    public List<Order> findByGuest(Guest guest) {
        return search(o -> o.getGuest().equals(guest));
    }

    public List<Order> findByRoom(Room room) {
        return search(o -> o.getRoom().equals(room));
    }

    public List<Order> findByNights(int nights) {
        return search(o -> o.getNights() == nights);
    }

    public List<Order> findByCheckIn(LocalDate date) {
        return search(o -> o.getCheckIn().equals(date));
    }

    /** האם החדר פנוי בכל הלילות שבטווח [from, to). */
    public boolean isRoomAvailable(Room room, LocalDate from, LocalDate to) {
        return orders.stream().noneMatch(o -> o.getRoom().equals(room) && o.overlaps(from, to));
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }
}
