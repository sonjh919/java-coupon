package coupon.marketing.ui;

import coupon.marketing.domain.MonthlyMemberBenefit;
import coupon.marketing.service.MarketingService;
import java.time.Month;
import java.time.Year;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MarketingController {

    private final MarketingService marketingService;

    public MarketingController(MarketingService marketingService) {
        this.marketingService = marketingService;
    }

    /**
     * 특정 연도(year)와 월(month)에 대해 월별 쿠폰 할인 금액이 가장 큰 회원을 조회하는 API
     * @param year 조회할 연도 (예: 2019)
     * @param month 조회할 월 (1~12)
     * @return 해당 월에 쿠폰 할인을 가장 많이 받은 회원 정보 (MonthlyMemberBenefit 객체)
     */
    @GetMapping("/marketing/max-coupon-discount-member")
    public MonthlyMemberBenefit findMaxCouponDiscountAmountMemberByMonth(@RequestParam("year") int year,
                                                                         @RequestParam("month") int month) {
        return marketingService.findMaxCouponDiscountAmountMemberByMonth(Year.of(year), Month.of(month));
    }
}
