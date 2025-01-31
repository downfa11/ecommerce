package ns.example.ecommerce.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import ns.example.ecommerce.ecommerce.domain.Keyword;
import ns.example.ecommerce.ecommerce.domain.Product;
import ns.example.ecommerce.ecommerce.domain.ProductGroup;
import ns.example.ecommerce.ecommerce.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/price")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/product")
    public ResponseEntity<Integer> addNewProduct(@RequestBody Product newProduct) {
        return ResponseEntity.ok(productService.SetNewProduct(newProduct));
    }

    @PostMapping("/product-group")
    public ResponseEntity<Integer> addNewProductGroup(@RequestBody ProductGroup newProductGroup) {
        return ResponseEntity.ok(productService.SetNewProductGroup(newProductGroup));
    }

    @PostMapping("/product-group/keyword")
    public ResponseEntity<Integer> addProductGroupToKeyword(@RequestParam String keyword, @RequestParam String productGroupId, @RequestParam double score) {
        return ResponseEntity.ok(productService.SetNewProductGroupToKeyword(keyword, productGroupId, score));
    }

    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<Keyword> getLowestPriceProductByKeyword(@PathVariable String keyword) {
        return ResponseEntity.ok(productService.GetLowestPriceProductByKeyword(keyword));
    }
}
