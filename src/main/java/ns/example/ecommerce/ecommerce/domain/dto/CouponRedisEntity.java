package ns.example.ecommerce.ecommerce.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import ns.example.ecommerce.ecommerce.domain.Coupon;
import ns.example.ecommerce.ecommerce.domain.enums.CouponType;
import ns.example.ecommerce.ecommerce.domain.enums.ErrorCode;
import ns.example.ecommerce.ecommerce.utils.exception.IssueException;


public record CouponRedisEntity(
        Long id,
        CouponType couponType,
        Integer totalQuantity,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime startDate,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime endDate) {

    public CouponRedisEntity(Coupon coupon){
        this(coupon.getId(), coupon.getCouponType(), coupon.getTotalQuantity(), coupon.getStartDate(), coupon.getEndDate());
    }

    private boolean availableDates(){
        LocalDateTime now = LocalDateTime.now();
        return startDate.isBefore(now) && endDate.isAfter(now);
    }

    public void checkValidDate(){
        if(!availableDates()){
            throw new IssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE);
        }
    }
}
