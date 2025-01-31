package ns.example.ecommerce.ecommerce.domain.enums;

public enum CouponType {
    TEST_COUPON("테스트 쿠폰", 10000),
    CULTURE_LAND("문화상품권 1만원권", 5000),
    BITCOIN("비트코인", 97919000);

    private final String title;
    private final int value;

    CouponType(String title, int value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public int getValue() {
        return value;
    }
}
