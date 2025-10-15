package coupon.coupon.ui;

import static java.util.stream.Collectors.toList;

import coupon.coupon.domain.Coupon;
import coupon.coupon.service.CouponService;
import coupon.coupon.service.MemberCouponService;
import coupon.coupon.ui.dto.CouponResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;
    private final MemberCouponService memberCouponService;

    public CouponController(CouponService couponService, MemberCouponService memberCouponService) {
        this.couponService = couponService;
        this.memberCouponService = memberCouponService;
    }

    /**
     * 특정 couponId에 해당하는 쿠폰 정보를 조회하는 API
     * @param couponId 조회할 쿠폰의 ID
     * @return 쿠폰 상세 정보 (CouponResponse DTO)
     */
    @GetMapping("{couponId:^\\d+$}")
    public CouponResponse getCoupon(@PathVariable("couponId") Long couponId) {
        return CouponResponse.from(couponService.getCoupon(couponId));
    }

    /**
     * 특정 couponId에 해당하는 쿠폰의 발급(issued) 수량을 조회하는 API
     * @param couponId 발급 수량을 조회할 쿠폰의 ID
     * @return 발급된 쿠폰 수량 (Long)
     */
    @GetMapping("{couponId:^\\d+$}/issued-count")
    public Long getCouponIssuedCount(@PathVariable("couponId") Long couponId) {
        Coupon coupon = couponService.getCoupon(couponId);
        return memberCouponService.findIssuedCouponCount(coupon.getId());
    }

    /**
     * 특정 couponId에 해당하는 쿠폰의 사용(used) 수량을 조회하는 API
     * @param couponId 사용 수량을 조회할 쿠폰의 ID
     * @return 사용된 쿠폰 수량 (Long)
     */
    @GetMapping("{couponId:^\\d+$}/used-count")
    public Long getCouponUsedCount(@PathVariable("couponId") Long couponId) {
        Coupon coupon = couponService.getCoupon(couponId);
        return memberCouponService.findUsedCouponCount(coupon.getId());
    }

    /**
     * 현재 발급 가능한(issuable) 모든 쿠폰 목록을 조회하는 API
     * @return 발급 가능한 쿠폰들의 리스트 (CouponResponse DTO 리스트)
     */
    @GetMapping("/issuable")
    public List<CouponResponse> findIssuableCoupons() {
        return couponService.findIssuableCoupons().stream()
                .map(CouponResponse::from)
                .collect(toList());
    }
}
