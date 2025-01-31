package ns.example.ecommerce.ecommerce.domain.LogData;

import lombok.Data;

@Data
public class AdLog {
    String userId;
    String productId;
    String advertisementId;
    String advertisementType;
    String watchingTime;
    String watchingDateTime;
}
