package ns.example.ecommerce.ecommerce.domain.stream;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdConversionResult {
    String adId;
    String userId;
    String orderId;
    Map<String, String> productInfo;
}