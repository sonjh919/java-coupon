package coupon.quizhelper;

import coupon.coupon.repository.CouponRepository;
import coupon.coupon.repository.MemberCouponRepository;
import coupon.quizhelper.dto.InitializeUsedRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuizHelperController {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;

    public QuizHelperController(CouponRepository couponRepository, MemberCouponRepository memberCouponRepository) {
        this.couponRepository = couponRepository;
        this.memberCouponRepository = memberCouponRepository;
    }

    /**
     * 특정 쿠폰의 발행 횟수를 0으로 초기화하는 API
     * @param couponId 초기화할 쿠폰의 ID
     */
    @PutMapping("/coupons/initialize-issue-count/{couponId}")
    public void initializeIssueCount(@PathVariable("couponId") Long couponId) {
        couponRepository.updateIssueCount(couponId, 0L);
    }

    /**
     * 특정 쿠폰의 사용 횟수를 0으로 초기화하는 API
     * @param couponId 초기화할 쿠폰의 ID
     */
    @PutMapping("/coupons/initialize-use-count/{couponId}")
    public void initializeUseCount(@PathVariable("couponId") Long couponId) {
        couponRepository.updateUseCount(couponId, 0L);
    }

    /**
     * 회원 쿠폰들의 사용 상태를 초기화하는 API
     * @param initializeUsedRequest 사용 상태를 초기화할 회원 쿠폰 ID 리스트를 포함한 요청 객체
     * 사용 여부를 false로 설정하고 사용 일시(usedAt)는 null로 초기화
     */
    @PutMapping("/member-coupons/initialize-used")
    public void initializeUseCount(@RequestBody InitializeUsedRequest initializeUsedRequest) {
        memberCouponRepository.updateUsedAndUsedAt(initializeUsedRequest.memberCouponIds(), false, null);
    }
}
