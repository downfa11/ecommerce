package ns.example.ecommerce.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import ns.example.ecommerce.ecommerce.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupon")
public class CouponController {
    private final CouponService couponService;


    @PostMapping("/issue/lua")
    public ResponseEntity<Void> requestCouponToLua(@RequestParam Long userId, @RequestParam Long couponId) {
        couponService.issueRequestToLua(couponId, userId);
        return ResponseEntity.ok().build();
    }
}