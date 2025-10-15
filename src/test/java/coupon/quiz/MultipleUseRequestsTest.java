package coupon.quiz;

import static org.assertj.core.api.Assertions.assertThat;
import static coupon.quiz.QuizHelper.getCoupon;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MultipleUseRequestsTest {

    private static final String BASE_URI = "http://localhost:8080";
    /**
     * 발급 수량 제한이 있는 쿠폰의 아이디
     */
    private static final Long USE_LIMIT_COUPON_ID = 351160L;
    /**
     * 동시에 사용 요청하는 스레드의 개수
     */
    private static final int CONCURRENT_REQUEST_COUNT = 5;
    /**
     * 쿠폰을 가진 회원 아이디
     */
    private static final Long MEMBER_ID = 1L;
    /**
     * 회원에게 발급된 회원 쿠폰의 아이디 목록. 여기서는 500001부터 500020까지 20개 쿠폰 ID
     */
    private static final List<Long> MEMBER_COUPON_IDS = LongStream.rangeClosed(500001L, 500020L).boxed().toList();

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URI;

        // 테스트 시작 전, 쿠폰의 사용 수량 초기화
        RestAssured.given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .put("/coupons/initialize-use-count/" + USE_LIMIT_COUPON_ID);

        // 회원 쿠폰들의 사용 여부 초기화 (모두 사용 안 함 상태로)
        RestAssured.given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .body("{\"memberCouponIds\": " + MEMBER_COUPON_IDS + "}")
                .put("/member-coupons/initialize-used");
    }

    /**
     * 여러 스레드가 동시에 쿠폰 사용 요청을 수행하는 테스트
     * 5개의 스레드가 병렬로 20개의 해당 회원 쿠폰에 대해 사용 요청을 보내며,
     * 최종적으로 성공한 사용 횟수는 5건, 전체 요청 수는 100건이 되어야 함을 검증
     */
    @Test
    void 동시_사용_요청() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicBoolean requestStart = new AtomicBoolean(false);

        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_REQUEST_COUNT);
        for (int i = 0; i < CONCURRENT_REQUEST_COUNT; i++) {
            executorService.submit(() -> useCoupon(requestCount, successCount, requestStart));
        }

        Thread.sleep(1000L);    // 스레드 대기 후 1초 지연시키고 요청 시작 신호를 켬
        requestStart.set(true);

        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        assertThat(successCount.get()).isEqualTo(5);
        assertThat(requestCount.get()).isEqualTo(100);

        Response couponResponse = getCoupon(USE_LIMIT_COUPON_ID);
        long useCount = couponResponse.body().jsonPath().getLong("useCount");
        assertThat(useCount).isEqualTo(5);
    }

    /**
     * 쿠폰 사용 요청을 수행하는 메서드
     * requestStart가 켜져야 실행을 시작하며,
     * 지정된 회원 쿠폰 ID 목록에 대해 사용 요청을 보냄
     * 요청 및 성공 카운트를 Atomic 변수로 안전하게 증가시킴
     *
     * @param requestCount 총 요청 횟수 기록용 AtomicInteger
     * @param successCount 성공한 요청 수 기록용 AtomicInteger
     * @param requestStart 요청 시작 신호를 받기 위한 AtomicBoolean
     */
    private static void useCoupon(AtomicInteger requestCount, AtomicInteger successCount, AtomicBoolean requestStart) {
        while (requestStart.get() == false) {
            // 요청 시작 신호 대기
        }

        for (Long memberCouponId : MEMBER_COUPON_IDS) {
            String requestBody = "{ \"memberCouponId\": " + memberCouponId + ", \"memberId\": " + MEMBER_ID + " }";
            Response response = RestAssured.given()
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                    .body(requestBody)
                    .post("/member-coupons/" + memberCouponId + "/use")
                    .then()
                    .extract().response();

            requestCount.incrementAndGet();

            if (response.getStatusCode() == HttpStatus.SC_OK) {
                successCount.incrementAndGet();
            }
        }
    }
}
