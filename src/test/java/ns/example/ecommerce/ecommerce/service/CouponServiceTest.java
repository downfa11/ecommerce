package ns.example.ecommerce.ecommerce.service;


import static ns.example.ecommerce.ecommerce.domain.enums.ErrorCode.COUPON_NOT_EXIST;
import static ns.example.ecommerce.ecommerce.domain.enums.ErrorCode.DUPLICATED_COUPON_ISSUE;
import static ns.example.ecommerce.ecommerce.domain.enums.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static ns.example.ecommerce.ecommerce.domain.enums.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;
import static ns.example.ecommerce.ecommerce.repository.RedisRepository.QUEUE_REQUEST_KEY;
import static ns.example.ecommerce.ecommerce.repository.RedisRepository.getIssueRequestKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.IntStream;
import ns.example.ecommerce.ecommerce.domain.Coupon;
import ns.example.ecommerce.ecommerce.domain.enums.CouponType;
import ns.example.ecommerce.ecommerce.domain.dto.CouponIssueRequest;
import ns.example.ecommerce.ecommerce.utils.exception.IssueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class CouponServiceTest {

    @Autowired
    CouponService couponService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RedisTemplate<String, String> redisTemplate;


    @BeforeEach
    void init(){
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }

    @Test
    @DisplayName("Coupon quantity validation 1")
    void 발급_가능_수량이_존재하면_true를_반환한다() {
        // given
        int totalIssueQuantity = 10;
        Long couponId = 1L;
        // when
        boolean result = couponService.availableTotalIssueQuantity(totalIssueQuantity, couponId);
        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("Coupon quantity validation 2")
    void 발급_가능_수량이_존재하면_false를_반환한다() {
        // given
        int totalIssueQuantity = 10;
        Long couponId = 1L;

        IntStream.range(0, totalIssueQuantity)
                .forEach(userId -> redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(userId)));
        // 이미 정해진 수량을 모두 발급함

        // when
        boolean result = couponService.availableTotalIssueQuantity(totalIssueQuantity, couponId);
        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("Coupon duplicated validation 1")
    void 발급한_내역에_사용자가_없으면_true를_반환한다() {
        // given
        Long couponId = 1L;
        Long userId = 1L;
        // when
        boolean result = couponService.availableUserIssueQuantity(couponId, userId);
        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("Coupon duplicated validation 2")
    void 발급한_내역에_사용자가_있으면_false를_반환한다() {
        // given
        Long couponId = 1L;
        Long userId = 1L;
        redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(userId));
        // when
        boolean result = couponService.availableUserIssueQuantity(couponId, userId);
        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("Coupon Create")
    void 쿠폰이_존재하지_않으면_예외_처리해야함(){
        // given
        Long couponId = 1L;
        Long userId = 1L;

        // when, then
        IssueException exception = Assertions.assertThrows(IssueException.class, () -> couponService.issueRequestToLua(couponId, userId));
        assertEquals(exception.getErrorCode(), COUPON_NOT_EXIST);
    }

    @Test
    void 발급_수량이_부족하면_예외를_반환해야함() {
        // given
        long userId = 1000;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.TEST_COUPON)
                .totalQuantity(10)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        saveCoupon(coupon);
        IntStream.range(0, coupon.getTotalQuantity()).forEach(idx ->
                redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(idx)));
        // when & then
        IssueException exception = Assertions.assertThrows(IssueException.class, () -> couponService.issueRequestToLua(coupon.getId(), userId));
        assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    void 이미_발급된_쿠폰이면_예외를_반환해야함() {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.TEST_COUPON)
                .totalQuantity(10)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();


        saveCoupon(coupon);
        redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(userId));
        // when & then
        IssueException exception = Assertions.assertThrows(IssueException.class, () -> couponService.issueRequestToLua(coupon.getId(), userId));
        assertEquals(exception.getErrorCode(), DUPLICATED_COUPON_ISSUE);
    }

    @Test
    void 발급_기한이_유효하지_않으면_예외를_반환해야함() {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.TEST_COUPON)
                .totalQuantity(10)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();

        saveCoupon(coupon);
        redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(userId));
        // when & then
        IssueException exception = Assertions.assertThrows(IssueException.class, () -> couponService.issueRequestToLua(coupon.getId(), userId));
        assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_DATE);
    }

    @Test
    void 쿠폰_발급시_기록() {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.TEST_COUPON)
                .totalQuantity(10)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();

        saveCoupon(coupon);
        // when
        couponService.issueRequestToLua(coupon.getId(), userId);
        // then
        Boolean isSaved = redisTemplate.opsForSet().isMember(getIssueRequestKey(coupon.getId()), String.valueOf(userId));
        assertTrue(isSaved);
    }

    @Test
    void 쿠폰_발급시_큐잉() throws JsonProcessingException {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.TEST_COUPON)
                .totalQuantity(10)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();

        saveCoupon(coupon);
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId(), userId);
        // when
        couponService.issueRequestToLua(coupon.getId(), userId);
        // then
        String savedIssueRequest = redisTemplate.opsForList().leftPop(QUEUE_REQUEST_KEY);
        assertEquals(new ObjectMapper().writeValueAsString(request), savedIssueRequest);
    }


    private void saveCoupon(Coupon coupon){

        try {
            String jsonCoupon = objectMapper.writeValueAsString(coupon);
            redisTemplate.opsForValue().set("coupon:" + coupon.getId(), jsonCoupon);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save coupon in Redis");
        }
    }
}
