package ns.example.ecommerce.ecommerce.service;

public interface CouponQueueService {
    void enqueue(String key, Object object);
    String dequeue(String key);
}
