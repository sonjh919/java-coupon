package coupon.quiz;

import static org.assertj.core.api.Assertions.assertThat;
import static coupon.quiz.QuizHelper.getCoupon;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MultipleIssueRequestsTest {

    private static final String BASE_URI = "http://localhost:8080";
    /**
     * 발급 수량 제한이 있는 쿠폰의 아이디
     */
    private static final Long ISSUE_LIMIT_COUPON_ID = 351159L;
    /**
     * 동시에 발급 요청하는 회원의 수
     */
    private static final int NUMBER_OF_MEMBERS = 10;
    /**
     * 회원당 발급 요청하는 쿠폰의 개수
     */
    private static final int COUPON_ISSUE_COUNT_PER_MEMBER = 20;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URI;

        // 테스트 시작 전, 쿠폰의 발급 수량을 초기화하여 테스트 환경을 동일하게 만듦
        RestAssured.given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .put("/coupons/initialize-issue-count/" + ISSUE_LIMIT_COUPON_ID);
    }

    /**
     * 여러 회원이 동시에 쿠폰을 발급 요청하는 시나리오 테스트
     * 총 10명의 회원이 각각 20개의 쿠폰 발급 요청을 동시 수행
     * - 성공적으로 발급된 쿠폰은 총 150건이어야 함 (발급 제한 반영)
     * - 전체 요청 수는 200건이어야 함 (10명 * 20개)
     * - API 호출 후 쿠폰의 실제 발급 수량이 150건과 일치하는지 검증
     */
    @Test
    void 동시_발급_요청() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger requestCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_MEMBERS);
        for (int i = 1; i <= NUMBER_OF_MEMBERS; i++) {  // 회원 번호는 1부터 시작한다.
            int memberId = i;
            executorService.submit(() -> {
                issueCoupon(memberId, requestCount, successCount);
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        assertThat(successCount.get()).isEqualTo(150);
        assertThat(requestCount.get()).isEqualTo(200);

        Response couponResponse = getCoupon(ISSUE_LIMIT_COUPON_ID);
        long issueCount = couponResponse.body().jsonPath().getLong("issueCount");
        assertThat(issueCount).isEqualTo(150);
    }

    /**
     * 회원이 쿠폰 발급을 여러 번 요청하는 요청 시뮬레이션 메서드
     * 요청 카운트와 성공 카운트를 AtomicInteger로 동기화하여 안전하게 증가시킴
     *
     * @param memberId 회원 아이디
     * @param requestCount 총 요청 횟수를 기록하는 AtomicInteger
     * @param successCount 성공적으로 발급된 횟수를 기록하는 AtomicInteger
     */
    private static void issueCoupon(int memberId, AtomicInteger requestCount, AtomicInteger successCount) {
        for (int count = 0; count < COUPON_ISSUE_COUNT_PER_MEMBER; count++) {
            String requestBody = "{ \"couponId\": " + ISSUE_LIMIT_COUPON_ID + ", \"memberId\": " + memberId + " }";
            Response response = RestAssured.given()
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                    .body(requestBody)
                    .post("/member-coupons")
                    .then()
                    .extract().response();

            requestCount.incrementAndGet();

            if (response.getStatusCode() == HttpStatus.SC_OK) {
                successCount.incrementAndGet();
            }
        }
    }

}
