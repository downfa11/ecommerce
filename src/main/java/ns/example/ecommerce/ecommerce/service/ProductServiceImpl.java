package ns.example.ecommerce.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import ns.example.ecommerce.ecommerce.domain.Keyword;
import ns.example.ecommerce.ecommerce.domain.Product;
import ns.example.ecommerce.ecommerce.domain.ProductGroup;
import ns.example.ecommerce.ecommerce.repository.RedisRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final RedisRepository redisRepository;
    private final ObjectMapper objectMapper;

    // 새로운 상품을 등록, 상품의 rank 반환
    public int SetNewProduct(Product newProduct) {
        redisRepository.zAdd(newProduct.getProductGroupId(), newProduct.getProductId(), newProduct.getPrice());
        return redisRepository.zRank(newProduct.getProductGroupId(), newProduct.getProductId()).intValue();
    }

    // ProductGroup 등록, 해당 Group의 상품 수 반환
    public int SetNewProductGroup(ProductGroup newProductGroup) {
        List<Product> product = newProductGroup.getProductList();
        String productId = product.get(0).getProductId();
        double price = product.get(0).getPrice();

        redisRepository.zAdd(newProductGroup.getProductGroupId(), productId, price);
        int productCnt = redisRepository.zCard(newProductGroup.getProductGroupId()).intValue();
        return productCnt;
    }

    // Keyword에 특정 Group 추가, 해당 Group의 rank 반환
    public int SetNewProductGroupToKeyword(String keyword, String prodGroupId, double score) {
        redisRepository.zAdd(keyword, prodGroupId, score);
        return redisRepository.zRank(keyword, prodGroupId).intValue();
    }

    // keyword에 기반한 최저 상품의 목록 반환
    public Keyword GetLowestPriceProductByKeyword(String keyword) {
        Keyword returnInfo = new Keyword();

        List<ProductGroup> tempProdGroup = GetProductGroupUsingKeyword(keyword);
        returnInfo.setKeyword(keyword);
        returnInfo.setProductGroupList(tempProdGroup);

        return returnInfo;
    }

    // keyword에 기반한 List<Group> 반환
    public List<ProductGroup> GetProductGroupUsingKeyword(String keyword) {
        List<ProductGroup> returnInfo = new ArrayList<>();
        List<String> prodGroupIdList = List.copyOf(redisRepository.zReverseRange(keyword, 0, 9));
        List<Product> tempProductList = new ArrayList<>();

        for (final String prodGroupId : prodGroupIdList) {
            ProductGroup tempProdGroup = new ProductGroup();

            Set prodAndPriceList = redisRepository.zRangeWithScores(prodGroupId, 0, 9);
            Iterator<Object> prodPricObj = prodAndPriceList.iterator();

            while (prodPricObj.hasNext()) {
                Map<String, Object> prodPriceMap = objectMapper.convertValue(prodPricObj.next(), Map.class);
                Product tempProduct = new Product();

                tempProduct.setProductId(prodPriceMap.get("value").toString());
                String score = String.valueOf(prodPriceMap.get("score"));
                tempProduct.setPrice(Integer.parseInt(score));
                tempProduct.setProductGroupId(prodGroupId);

                tempProductList.add(tempProduct);
            }

            tempProdGroup.setProductGroupId(prodGroupId);
            tempProdGroup.setProductList(tempProductList);
            returnInfo.add(tempProdGroup);
        }

        return returnInfo;
    }
}
