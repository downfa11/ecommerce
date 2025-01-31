package ns.example.ecommerce.ecommerce.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductGroup {
    private String productGroupId;
    private List<Product> productList;
}
