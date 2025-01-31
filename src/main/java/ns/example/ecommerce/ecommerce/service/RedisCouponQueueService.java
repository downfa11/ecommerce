package ns.example.ecommerce.ecommerce.service;


import static ns.example.ecommerce.ecommerce.repository.RedisRepository.QUEUE_REQUEST_KEY;

import lombok.RequiredArgsConstructor;
import ns.example.ecommerce.ecommerce.repository.RedisRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisCouponQueueService implements CouponQueueService {

    private final RedisRepository redisRepository;

    @Override
    public void enqueue(String key, Object object) {
        redisRepository.rPush(QUEUE_REQUEST_KEY, String.valueOf(object));
    }

    @Override
    public String dequeue(String key) {
        return redisRepository.rPop(key);
    }
}
