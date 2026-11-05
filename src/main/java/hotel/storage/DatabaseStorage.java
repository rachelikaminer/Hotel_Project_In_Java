package hotel.storage;

import hotel.manager.Hotel;
import hotel.model.BusinessGuest;
import hotel.model.Guest;
import hotel.model.Order;
import hotel.model.Room;
import hotel.model.RoomLevel;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * שמירה וטעינה של נתוני המלון במסד נתונים H2.
 */
public class DatabaseStorage {
    private final String url;

    public DatabaseStorage(String url) {
        this.url = url;
    }

    /** האם כבר נשמר מלון במסד הנתונים. */
    public boolean hasData() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url)) {
            createTables(conn);
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM hotel_info")) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** שומר את כל נתוני המלון. אם משהו נכשל – שום שינוי לא נשמר. */
    public void save(Hotel hotel) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url)) {
            createTables(conn);
            conn.setAutoCommit(false);
            try {
                clearTables(conn);
                insertHotelInfo(conn, hotel);
                insertRooms(conn, hotel);
                insertGuests(conn, hotel);
                insertOrders(conn, hotel);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Hotel load() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url)) {
            createTables(conn);
            Hotel hotel = loadHotelInfo(conn);
            loadRooms(conn, hotel);
            loadGuests(conn, hotel);
            loadOrders(conn, hotel);
            return hotel;
        }
    }

    private void createTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS hotel_info (
                        name VARCHAR(100) NOT NULL,
                        address VARCHAR(200),
                        phone VARCHAR(30),
                        last_order_number INT NOT NULL
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS rooms (
                        room_number INT PRIMARY KEY,
                        floor INT NOT NULL,
                        room_level VARCHAR(20) NOT NULL
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS guests (
                        id VARCHAR(20) PRIMARY KEY,
                        first_name VARCHAR(50) NOT NULL,
                        last_name VARCHAR(50),
                        address VARCHAR(200),
                        phone VARCHAR(30),
                        email VARCHAR(100),
                        num_visits INT NOT NULL,
                        business_code VARCHAR(30),
                        discount_percent DOUBLE PRECISION
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS orders (
                        order_number INT PRIMARY KEY,
                        guest_id VARCHAR(20) NOT NULL REFERENCES guests(id),
                        room_number INT NOT NULL REFERENCES rooms(room_number),
                        check_in DATE NOT NULL,
                        nights INT NOT NULL
                    )""");
        }
    }

    private void clearTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            // קודם orders, כי היא מפנה ל-guests ול-rooms
            st.executeUpdate("DELETE FROM orders");
            st.executeUpdate("DELETE FROM guests");
            st.executeUpdate("DELETE FROM rooms");
            st.executeUpdate("DELETE FROM hotel_info");
        }
    }

    private void insertHotelInfo(Connection conn, Hotel hotel) throws SQLException {
        String sql = "INSERT INTO hotel_info (name, address, phone, last_order_number) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hotel.getName());
            ps.setString(2, hotel.getAddress());
            ps.setString(3, hotel.getPhone());
            ps.setInt(4, hotel.getOrderManager().getLastOrderNumber());
            ps.executeUpdate();
        }
    }

    private void insertRooms(Connection conn, Hotel hotel) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, floor, room_level) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Room room : hotel.getRoomManager().getRooms()) {
                ps.setInt(1, room.getNumber());
                ps.setInt(2, room.getFloor());
                ps.setString(3, room.getLevel().name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertGuests(Connection conn, Hotel hotel) throws SQLException {
        String sql = """
                INSERT INTO guests (id, first_name, last_name, address, phone, email,
                                    num_visits, business_code, discount_percent)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Guest guest : hotel.getGuestManager().getGuests()) {
                ps.setString(1, guest.getId());
                ps.setString(2, guest.getFirstName());
                ps.setString(3, guest.getLastName());
                ps.setString(4, guest.getAddress());
                ps.setString(5, guest.getPhone());
                ps.setString(6, guest.getEmail());
                ps.setInt(7, guest.getNumVisits());
                if (guest instanceof BusinessGuest businessGuest) {
                    ps.setString(8, businessGuest.getBusinessCode());
                    ps.setDouble(9, businessGuest.getDiscountPercent());
                } else {
                    ps.setNull(8, Types.VARCHAR);
                    ps.setNull(9, Types.DOUBLE);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertOrders(Connection conn, Hotel hotel) throws SQLException {
        String sql = "INSERT INTO orders (order_number, guest_id, room_number, check_in, nights) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Order order : hotel.getOrderManager().getOrders()) {
                ps.setInt(1, order.getOrderNumber());
                ps.setString(2, order.getGuest().getId());
                ps.setInt(3, order.getRoom().getNumber());
                ps.setDate(4, Date.valueOf(order.getCheckIn()));
                ps.setInt(5, order.getNights());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Hotel loadHotelInfo(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT name, address, phone, last_order_number FROM hotel_info")) {
            if (!rs.next()) {
                throw new SQLException("אין נתוני מלון במסד הנתונים");
            }
            Hotel hotel = new Hotel(rs.getString("name"), rs.getString("address"), rs.getString("phone"));
            hotel.getOrderManager().restoreLastOrderNumber(rs.getInt("last_order_number"));
            return hotel;
        }
    }

    private void loadRooms(Connection conn, Hotel hotel) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT room_number, floor, room_level FROM rooms ORDER BY room_number")) {
            while (rs.next()) {
                hotel.addRoom(new Room(rs.getInt("room_number"), rs.getInt("floor"),
                        RoomLevel.valueOf(rs.getString("room_level"))));
            }
        }
    }

    private void loadGuests(Connection conn, Hotel hotel) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM guests ORDER BY id")) {
            while (rs.next()) {
                String businessCode = rs.getString("business_code");
                Guest guest;
                if (businessCode == null) {
                    guest = new Guest(rs.getString("id"), rs.getString("first_name"), rs.getString("last_name"),
                            rs.getString("address"), rs.getString("phone"), rs.getString("email"));
                } else {
                    BusinessGuest businessGuest = new BusinessGuest(rs.getString("id"), rs.getString("first_name"),
                            rs.getString("last_name"), rs.getString("address"), rs.getString("phone"),
                            rs.getString("email"), businessCode);
                    businessGuest.setDiscountPercent(rs.getDouble("discount_percent"));
                    guest = businessGuest;
                }
                guest.setNumVisits(rs.getInt("num_visits"));
                hotel.registerGuest(guest);
            }
        }
    }

    private void loadOrders(Connection conn, Hotel hotel) throws SQLException {
        String sql = "SELECT order_number, guest_id, room_number, check_in, nights FROM orders ORDER BY order_number";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Guest guest = hotel.getGuestManager().findById(rs.getString("guest_id")).orElseThrow();
                Room room = hotel.getRoomManager().findByNumber(rs.getInt("room_number")).orElseThrow();
                Order order = new Order(rs.getInt("order_number"), guest, room,
                        rs.getDate("check_in").toLocalDate(), rs.getInt("nights"));
                hotel.getOrderManager().restoreOrder(order);
            }
        }
    }
}