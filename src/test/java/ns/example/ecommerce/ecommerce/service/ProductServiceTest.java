package ns.example.ecommerce.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ns.example.ecommerce.ecommerce.domain.Keyword;
import ns.example.ecommerce.ecommerce.domain.Product;
import ns.example.ecommerce.ecommerce.domain.ProductGroup;
import ns.example.ecommerce.ecommerce.repository.RedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

@SpringBootTest
public class ProductServiceTest {
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Mock
    private RedisRepository redisRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductGroup productGroup;

    @BeforeEach
    void init() {
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);

        product = new Product("test-product", "test-group", 1000);
        productGroup = new ProductGroup("test-group", List.of(product));
    }

    @Test
    void 새로운_상품을_등록() {
        // given
        when(redisRepository.zAdd(product.getProductGroupId(), product.getProductId(), product.getPrice())).thenReturn(true);
        when(redisRepository.zRank(product.getProductGroupId(), product.getProductId())).thenReturn(0L);
        // when
        int rank = productService.SetNewProduct(product);
        // then
        assertEquals(0, rank);
        verify(redisRepository).zAdd(product.getProductGroupId(), product.getProductId(), product.getPrice());
        verify(redisRepository).zRank(product.getProductGroupId(), product.getProductId());
    }

    @Test
    void 새로운_상품_그룹을_등록() {
        // given
        when(redisRepository.zAdd(productGroup.getProductGroupId(), product.getProductId(), product.getPrice())).thenReturn(true);
        when(redisRepository.zCard(productGroup.getProductGroupId())).thenReturn(1L);

        // when
        int productCount = productService.SetNewProductGroup(productGroup);

        // then
        assertEquals(1, productCount);
        verify(redisRepository).zAdd(productGroup.getProductGroupId(), product.getProductId(), product.getPrice());
        verify(redisRepository).zCard(productGroup.getProductGroupId());
    }

    @Test
    void 키워드에_특정_상품_그룹을_등록() {
        String keyword = "testKeyword";
        String productGroupId = "test-group";
        double score = 20.0;
        // given
        when(redisRepository.zAdd(keyword, productGroupId, score)).thenReturn(true);
        when(redisRepository.zRank(keyword, productGroupId)).thenReturn(0L);
        // when
        int rank = productService.SetNewProductGroupToKeyword(keyword, productGroupId, score);
        // then
        assertEquals(0, rank);
        verify(redisRepository).zAdd(keyword, productGroupId, score);
        verify(redisRepository).zRank(keyword, productGroupId);
    }

    @Test
    void 키워드에_따른_상품의_최저가_리스트를_조회() {
        String keyword = "testKeyword";
        List<String> productGroupIds = List.of("test-group");
        Set<TypedTuple<String>> productData = new HashSet<>();
        productData.add(new DefaultTypedTuple<>("test-product", 1000.0));

        // given
        when(redisRepository.zReverseRange(keyword, 0, 9)).thenReturn(new HashSet<>(productGroupIds));
        when(redisRepository.zRangeWithScores("test-group", 0, 9)).thenReturn(productData);
        // when
        Keyword result = productService.GetLowestPriceProductByKeyword(keyword);
        // then
        assertNotNull(result);
        assertEquals(keyword, result.getKeyword());
        assertEquals(1, result.getProductGroupList().size());
        assertEquals(1, result.getProductGroupList().get(0).getProductList().size());
        assertEquals("test-product", result.getProductGroupList().get(0).getProductList().get(0).getProductId());
    }
}

