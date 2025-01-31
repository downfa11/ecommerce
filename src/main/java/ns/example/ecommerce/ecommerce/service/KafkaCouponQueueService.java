package ns.example.ecommerce.ecommerce.service;


import org.springframework.stereotype.Service;

@Service
public class KafkaCouponQueueService implements CouponQueueService {


    @Override
    public void enqueue(String key, Object object) {

    }

    @Override
    public String dequeue(String key) { return ""; }
    // todo
}
