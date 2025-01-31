package ns.example.ecommerce.ecommerce.domain.LogData;

import java.util.ArrayList;
import java.util.Map;
import lombok.Data;

@Data
public class PurchaseLog {
    String orderId;
    String userId;
    ArrayList<Map<String, String>> productInfo;
    String purchasedDate;
}
