package ns.example.ecommerce.ecommerce.domain;


import static ns.example.ecommerce.ecommerce.domain.enums.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static ns.example.ecommerce.ecommerce.domain.enums.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ns.example.ecommerce.ecommerce.domain.enums.CouponType;
import ns.example.ecommerce.ecommerce.utils.exception.IssueException;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {

    private Long id;

    @Enumerated(value=EnumType.STRING)
    private CouponType couponType;
    
    private Integer totalQuantity;
    private Integer discountAmount;
    private Integer issuedQuantity;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public boolean availableIssueQuantity() {
        if (totalQuantity == null) {
            return true;
        }
        return totalQuantity > issuedQuantity;
    }

    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return startDate.isBefore(now) && endDate.isAfter(now);
    }

    public boolean isIssueComplete() {
        LocalDateTime now = LocalDateTime.now();
        return endDate.isBefore(now) || !availableIssueQuantity();
    }

    public void issue() {
        if (!availableIssueQuantity())
            throw new IssueException(INVALID_COUPON_ISSUE_QUANTITY, "total : %s, issued: %s".formatted(totalQuantity, issuedQuantity));
        if (!availableIssueDate())
            throw new IssueException(INVALID_COUPON_ISSUE_DATE, "request : %s, startDate: %s, endDate: %s".formatted(LocalDateTime.now(), startDate, endDate));

        issuedQuantity++;
    }
}
