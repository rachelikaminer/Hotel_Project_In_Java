package hotel.manager;

import hotel.model.Guest;
import hotel.model.Order;
import hotel.model.Room;
import hotel.model.RoomStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * בית המלון – נקודת הכניסה לכל הפעולות על חדרים, לקוחות והזמנות.
 */
public class Hotel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String address;
    private final String phone;
    private final RoomManager roomManager = new RoomManager();
    private final GuestManager guestManager = new GuestManager();
    private final OrderManager orderManager = new OrderManager();

    public Hotel(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public void addRoom(Room room) {
        roomManager.addRoom(room);
    }

    public boolean registerGuest(Guest guest) {
        return guestManager.addGuest(guest);
    }

    /**
     * הזמנת חדר ללקוח רשום.
     *
     * @throws IllegalArgumentException אם הלקוח או החדר לא קיימים
     * @throws IllegalStateException    אם החדר תפוס באחד הלילות המבוקשים
     */
    public Order bookRoom(String guestId, int roomNumber, LocalDate checkIn, int nights) {
        Guest guest = guestManager.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("לא נמצא לקוח עם ת.ז. " + guestId));
        Room room = roomManager.findByNumber(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException("לא נמצא חדר מספר " + roomNumber));
        if (nights < 1) {
            throw new IllegalArgumentException("מספר הלילות חייב להיות לפחות 1");
        }
        if (!orderManager.isRoomAvailable(room, checkIn, checkIn.plusDays(nights))) {
            throw new IllegalStateException("חדר " + roomNumber + " תפוס בחלק מהתאריכים המבוקשים");
        }
        Order order = orderManager.createOrder(guest, room, checkIn, nights);
        guest.incrementVisits();
        return order;
    }

    public boolean cancelOrder(int orderNumber) {
        return orderManager.removeOrder(orderNumber);
    }

    public RoomStatus getRoomStatus(Room room, LocalDate date) {
        boolean occupied = orderManager.findByRoom(room).stream().anyMatch(o -> o.isActiveOn(date));
        return occupied ? RoomStatus.OCCUPIED : RoomStatus.AVAILABLE;
    }

    public List<Room> getRoomsByStatus(RoomStatus status, LocalDate date) {
        return roomManager.search(room -> getRoomStatus(room, date) == status);
    }

    /** חדרים שפנויים בכל הלילות מתאריך הכניסה ועד העזיבה. */
    public List<Room> findAvailableRooms(LocalDate checkIn, int nights) {
        LocalDate checkOut = checkIn.plusDays(nights);
        return roomManager.search(room -> orderManager.isRoomAvailable(room, checkIn, checkOut));
    }

    /** לקוחות שיש להם הזמנה נוכחית או עתידית. */
    public List<Guest> getActiveGuests(LocalDate today) {
        return guestManager.search(guest -> hasUpcomingOrders(guest, today));
    }

    /**
     * מחיקת לקוח. לא ניתן למחוק לקוח שיש לו הזמנה נוכחית או עתידית.
     *
     * @return false אם הלקוח לא נמצא
     */
    public boolean removeGuest(String guestId, LocalDate today) {
        Guest guest = guestManager.findById(guestId).orElse(null);
        if (guest == null) {
            return false;
        }
        if (hasUpcomingOrders(guest, today)) {
            throw new IllegalStateException("לא ניתן למחוק לקוח שיש לו הזמנות פעילות");
        }
        return guestManager.removeGuest(guestId);
    }

    private boolean hasUpcomingOrders(Guest guest, LocalDate today) {
        return orderManager.findByGuest(guest).stream().anyMatch(o -> o.getCheckOut().isAfter(today));
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public RoomManager getRoomManager() {
        return roomManager;
    }

    public GuestManager getGuestManager() {
        return guestManager;
    }

    public OrderManager getOrderManager() {
        return orderManager;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | טל' %s | %d חדרים, %d לקוחות, %d הזמנות",
                name, address, phone, roomManager.getRooms().size(),
                guestManager.getGuests().size(), orderManager.getOrders().size());
    }
}
