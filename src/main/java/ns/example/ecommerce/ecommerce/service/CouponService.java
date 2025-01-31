package ns.example.ecommerce.ecommerce.service;


import static ns.example.ecommerce.ecommerce.repository.RedisRepository.QUEUE_REQUEST_KEY;
import static ns.example.ecommerce.ecommerce.repository.RedisRepository.getIssueRequestKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ns.example.ecommerce.ecommerce.domain.Coupon;
import ns.example.ecommerce.ecommerce.domain.dto.CouponIssueRequest;
import ns.example.ecommerce.ecommerce.domain.dto.CouponRedisEntity;
import ns.example.ecommerce.ecommerce.domain.enums.ErrorCode;
import ns.example.ecommerce.ecommerce.utils.exception.IssueException;
import ns.example.ecommerce.ecommerce.repository.RedisRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CouponService {

    private final RedisRepository redisRepository;
    private final ObjectMapper objectMapper;
    private final CouponQueueService couponQueueService;


    public CouponService(
            RedisRepository redisRepository, ObjectMapper objectMapper,
            @Qualifier("redisCouponQueueService") CouponQueueService couponQueueService) {
        this.redisRepository = redisRepository;
        this.objectMapper = objectMapper;
        this.couponQueueService = couponQueueService;
    }

    // 쿠폰 조회
    @Cacheable(cacheNames = "couponCache")
    public CouponRedisEntity getCouponCache(Long couponId){
        Coupon coupon = redisRepository.getCouponById(couponId);
        return new CouponRedisEntity(coupon);
    }

/*    쿠폰 발급 요청 - Redisson 분산 락
    public void issueRequestToLock(Long couponId, Long userId){
        CouponRedisEntity coupon = getCouponCache(couponId);
        coupon.checkValidDate();

        distributedLock.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            checkCouponQuantity(coupon, userId);
            issueRequest(couponId, userId);
        });
    }*/


    // 쿠폰 발급 요청 - RedisScript EVAL을 통한 트랜잭션 처리
    public void issueRequestToLua(Long couponId, Long userId){
        CouponRedisEntity coupon = getCouponCache(couponId);
        coupon.checkValidDate();
        issueRequest(couponId, userId, coupon.totalQuantity());
    }

    // Validation
    public void checkCouponQuantity(CouponRedisEntity coupon, Long userId){
        Long couponId = coupon.id();
        if(!availableTotalIssueQuantity(coupon.totalQuantity(), couponId))
            throw new IssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "coupon %s, userId %s".formatted(couponId, userId));
        if(!availableUserIssueQuantity(couponId, userId))
            throw new IssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "coupon %s, userId %s".formatted(couponId, userId));
    }

    // 쿠폰 발급
    private void issueRequest(Long couponId, Long userId){
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
        try {
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            // 요청의 발급 수량 확인, 제어를 위한 IssueRequestKey(couponId)

            couponQueueService.enqueue(QUEUE_REQUEST_KEY, objectMapper.writeValueAsString(issueRequest));
            // 쿠폰 발급 큐에 적재

        }catch (JsonProcessingException e){
            throw new IssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(issueRequest));
        }
    }

    private void issueRequest(Long couponId, Long userId, Integer totalQuantity)
    {
        if(totalQuantity==null)
            redisRepository.issueRequest(couponId, userId, Integer.MAX_VALUE);
        else
            redisRepository.issueRequest(couponId,userId,totalQuantity);
    }


    // 해당 사용자가 쿠폰 수량 제한을 만족하는지 검사합니다.
    public boolean availableTotalIssueQuantity(Integer totalQuantity, Long couponId){
        if(totalQuantity==null) return true;

        String key = getIssueRequestKey(couponId);
        return totalQuantity > redisRepository.sCard(key);
    }


    // 중복 발급 여부를 검사합니다.
    public boolean availableUserIssueQuantity(Long couponId, Long userId){
        String key = getIssueRequestKey(couponId);
        return !redisRepository.sIsMember(key, String.valueOf(userId));
    }
}
