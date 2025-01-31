package ns.example.ecommerce.ecommerce.domain.LogData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseProductLog {
    String orderId;
    String userId;
    String productId;
    String purchasedDate;
    String price;
}
