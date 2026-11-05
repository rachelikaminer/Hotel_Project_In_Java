package hotel.model;

import java.io.Serial;

/**
 * לקוח עסקי – מקבל הנחה על כל הזמנה.
 */
public class BusinessGuest extends Guest {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final double DEFAULT_DISCOUNT_PERCENT = 10.0;

    private final String businessCode;
    private double discountPercent = DEFAULT_DISCOUNT_PERCENT;

    public BusinessGuest(String id, String firstName, String lastName, String address, String phone,
                         String email, String businessCode) {
        super(id, firstName, lastName, address, phone, email);
        this.businessCode = requireText(businessCode, "קוד לקוח עסקי");
    }

    public String getBusinessCode() {
        return businessCode;
    }

    @Override
    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("אחוז הנחה חייב להיות בין 0 ל-100");
        }
        this.discountPercent = discountPercent;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | לקוח עסקי %s (הנחה %.0f%%)", businessCode, discountPercent);
    }
}
