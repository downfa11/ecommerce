package ns.example.ecommerce.ecommerce.repository;

import static ns.example.ecommerce.ecommerce.domain.enums.ErrorCode.NOT_EXIST_KEY_IN_REDIS;
import static ns.example.ecommerce.ecommerce.domain.enums.ErrorCode.NOT_HAVE_DATA_IN_KEY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import ns.example.ecommerce.ecommerce.domain.Coupon;
import ns.example.ecommerce.ecommerce.domain.dto.CouponIssueRequest;
import ns.example.ecommerce.ecommerce.domain.enums.ErrorCode;
import ns.example.ecommerce.ecommerce.utils.exception.IssueException;
import ns.example.ecommerce.ecommerce.utils.RedisScriptCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RedisRepository {

    private static final String FAILED_TO_SAVE_COUPON_IN_REDIS_ERROR = "Failed to save coupon in Redis";
    private static final String FAILED_TO_RETRIEVE_COUPON_FROM_REDIS_ERROR = "Failed to retrieve coupon from Redis";

    public static final String COUPON_REQUEST_KEY = "issue.request.coupondId";
    public static final String QUEUE_REQUEST_KEY = "issue.request.queue";


    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<String> issueScript = issueRequestScript();
    private final ObjectMapper objectMapper;


    public static String getIssueRequestKey(Long coupondId){
        return COUPON_REQUEST_KEY+ ":" + coupondId;
    }

    public void saveCoupon(Coupon coupon){
        try {
            String jsonCoupon = objectMapper.writeValueAsString(coupon);
            redisTemplate.opsForValue().set("coupon:" + coupon.getId(), jsonCoupon);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(FAILED_TO_SAVE_COUPON_IN_REDIS_ERROR);
        }
    }

    public Coupon getCouponById(Long couponId){
        try {
            String jsonCoupon = redisTemplate.opsForValue().get("coupon:" + couponId);
            if (jsonCoupon != null) {
                return objectMapper.readValue(jsonCoupon, Coupon.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(FAILED_TO_RETRIEVE_COUPON_FROM_REDIS_ERROR);
        }
        return null;
    }


    public Boolean zAdd(String key, String value, double score){
        // Sorted Set에 없는 경우만 add
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

    public Long zCard(String key){
        return redisTemplate.opsForZSet().zCard(key);
    }

    public Long zRank(String key, String value){
        return redisTemplate.opsForZSet().rank(key, value);
    }

    public Set<String> zReverseRange(String key, int start, int end){
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    public Set<TypedTuple<String>> zRangeWithScores(String key, int start, int end){
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    public Long sAdd(String key, String value){
        return redisTemplate.opsForSet().add(key, value);
    }

    public Long sCard(String key){
        return redisTemplate.opsForSet().size(key);
    }

    public Boolean sIsMember(String key, String value){
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long rPush(String key, String value){
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public String rPop(String key) { return redisTemplate.opsForList().rightPop(key); }

    public void issueRequest(Long couponId, Long userId, Integer totalQuantity) {
        String requestKey = getIssueRequestKey(couponId);
        CouponIssueRequest couponIssueRequest = new CouponIssueRequest(couponId, userId);
        String queueKey = QUEUE_REQUEST_KEY;
        try {
            String code = redisTemplate.execute(
                    issueScript,
                    List.of(requestKey, queueKey),
                    String.valueOf(userId),
                    String.valueOf(totalQuantity),
                    objectMapper.writeValueAsString(couponIssueRequest)
            );
            RedisScriptCode.checkRequestResult(RedisScriptCode.find(code));
        } catch (JsonProcessingException e) {
            throw new IssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(couponIssueRequest));
        }
    }

    private RedisScript<String> issueRequestScript(){
        String script = """
                if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
                    return '2'
                end
                
                if tonumber(ARGV[2]) > redis.call('SCARD', KEYS[1]) then
                    redis.call('SADD', KEYS[1], ARGV[1])
                    redis.call('RPUSH', KEYS[2], ARGC[3])
                    return '1'
                end
                
                return '3'
                """;
        return RedisScript.of(script, String.class);
    }

    public Set GetZsetValue(String key) {
        Set myTempSet = redisTemplate.opsForZSet().rangeWithScores(key, 0, 9);
        return myTempSet;
    }

    public Set GetZsetValueWithStatus(String key) throws Exception {
        Set myTempSet = redisTemplate.opsForZSet().rangeWithScores(key, 0, 9);
        if (myTempSet.size()<1) throw new IssueException(NOT_HAVE_DATA_IN_KEY);
        return myTempSet;
    }

    public Set GetZsetValueWithSpecificException(String key) throws Exception {
        Set myTempSet = redisTemplate.opsForZSet().rangeWithScores(key, 0, 9);
        if (myTempSet.size() < 1) throw new IssueException(NOT_EXIST_KEY_IN_REDIS);

        return myTempSet;
    }

    public void DeleteKey(String key) {
        redisTemplate.delete(key);
    }
}
