package coupon.coupon.ui;

import static java.util.stream.Collectors.toList;

import coupon.coupon.service.CouponIssuer;
import coupon.coupon.service.MemberCouponService;
import coupon.coupon.ui.dto.IssueCouponRequest;
import coupon.coupon.ui.dto.MemberCouponResponse;
import coupon.coupon.ui.dto.UseCouponRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member-coupons")
public class MemberCouponController {

    private final CouponIssuer couponIssuer;
    private final MemberCouponService memberCouponService;

    public MemberCouponController(CouponIssuer couponIssuer, MemberCouponService memberCouponService) {
        this.couponIssuer = couponIssuer;
        this.memberCouponService = memberCouponService;
    }

    /**
     * 회원에게 쿠폰을 발급하는 API
     * @param request 쿠폰 발급 요청 정보 (쿠폰 ID, 회원 ID 포함)
     * @return 발급된 회원 쿠폰의 ID(Long)
     */
    @PostMapping
    public Long issueCoupon(@RequestBody IssueCouponRequest request) {
        return couponIssuer.issueCoupon(request.couponId(), request.memberId()).getId();
    }

    /**
     * 특정 회원이 보유하고 있으면서 사용할 수 있는 쿠폰 목록을 조회하는 API
     * @param memberId 조회 대상 회원의 ID
     * @return 해당 회원의 사용 가능한 쿠폰 리스트 (MemberCouponResponse DTO 리스트)
     */
    @GetMapping("/by-member-id")
    public List<MemberCouponResponse> getMemberCoupons(@RequestParam("memberId") Long memberId) {
        return memberCouponService.findUsableMemberCoupons(memberId).stream()
                .map(MemberCouponResponse::from)
                .collect(toList());
    }

    /**
     * 특정 회원 쿠폰을 사용 처리하는 API
     * @param memberCouponId 사용하려는 회원 쿠폰의 ID (경로 변수)
     * @param useCouponRequest 사용 요청 정보 (회원 쿠폰 ID, 회원 ID 포함)
     * @throws IllegalArgumentException 요청 경로와 본문에 전달된 회원 쿠폰 ID가 다르면 예외 발생
     */
    @PostMapping("/{memberCouponId:^\\d+$}/use")
    public void useCoupon(@PathVariable Long memberCouponId, @RequestBody UseCouponRequest useCouponRequest) {
        if (!memberCouponId.equals(useCouponRequest.memberCouponId())) {
            throw new IllegalArgumentException("잘못된 쿠폰 번호입니다.");
        }
        memberCouponService.useCoupon(useCouponRequest.memberId(), useCouponRequest.memberCouponId());
    }
}
