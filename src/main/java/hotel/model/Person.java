package hotel.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * אדם במערכת. מזוהה באופן ייחודי לפי מספר תעודת הזהות.
 */
public class Person implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
    private String email;

    public Person(String id, String firstName, String lastName, String address, String phone, String email) {
        this.id = requireText(id, "תעודת זהות");
        this.firstName = requireText(firstName, "שם פרטי");
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    protected static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " הוא שדה חובה");
        }
        return value.trim();
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = requireText(firstName, "שם פרטי");
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return lastName == null || lastName.isBlank() ? firstName : firstName + " " + lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // שוויון לפי ת.ז. בלבד, כך ששינוי בפרטים אחרים לא משנה את זהות האדם
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s (ת.ז. %s)", getFullName(), id);
    }
}
