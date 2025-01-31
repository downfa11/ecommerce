package ns.example.ecommerce.ecommerce.service;

import ns.example.ecommerce.ecommerce.domain.Keyword;
import ns.example.ecommerce.ecommerce.domain.Product;
import ns.example.ecommerce.ecommerce.domain.ProductGroup;

public interface ProductService {

    // 새로운 상품을 등록, 상품의 rank 반환
    int SetNewProduct(Product newProduct);

    // ProductGroup 등록, 해당 Group의 상품 수 반환
    int SetNewProductGroup(ProductGroup newProductGroup);

    // Keyword에 특정 Group 추가, 해당 Group의 rank 반환
    int SetNewProductGroupToKeyword (String keyword, String prodGroupId, double score);

    // keyword에 기반한 최저 상품의 목록 반환
    Keyword GetLowestPriceProductByKeyword(String keyword);
}
