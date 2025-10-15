package coupon.quiz;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.restassured.RestAssured;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IndexPerformanceTest {

    private static final String BASE_URI = "http://localhost:8080";
    private static final Long MIN_COUPON_ID = 1L;
    private static final Long MAX_COUPON_ID = 351160L;
    private static final Long MIN_MEMBER_ID = 1L;
    private static final Long MAX_MEMBER_ID = 250000L;
    private static final int THREAD_COUNT = 10;
    private static final int TEST_DURATION_SECONDS = 10;
    private static final long MILLISECONDS_IN_SECOND = 1000L;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    /**
     * 쿠폰의 발급 수량 조회 API 성능 테스트
     * 여러 스레드를 사용해 동시에 다양한 쿠폰 ID에 대해 발급 수량을 조회하고,
     * 평균 응답 시간을 측정하여 100ms 이하인지 검증함
     */
    @Test
    void 쿠폰의_발급_수량_조회() throws InterruptedException {
        AtomicBoolean running = new AtomicBoolean(false);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicLong totalElapsedTime = new AtomicLong(0);

        int statusCode = RestAssured.get("/coupons/" + ThreadLocalRandom.current()
                .nextLong(MIN_COUPON_ID, MAX_COUPON_ID + 1) + "/issued-count").statusCode();
        assertThat(statusCode).withFailMessage("쿠폰의 발급 수량 조회 API 호출에 실패했습니다. 테스트 대상 서버가 실행중인지 확인해 주세요.").isEqualTo(200);

        executeMultipleRequests(running, requestCount, totalElapsedTime,
                () -> RestAssured.get("/coupons/" + ThreadLocalRandom.current()
                        .nextLong(MIN_COUPON_ID, MAX_COUPON_ID + 1) + "/issued-count"));

        System.out.println("Total request count: " + requestCount.get());
        System.out.println("Total elapsed time: " + totalElapsedTime.get() + "ms");

        long averageElapsedTime = totalElapsedTime.get() / requestCount.get();
        System.out.println("Average elapsed time: " + averageElapsedTime + "ms");

        assertThat(averageElapsedTime).isLessThanOrEqualTo(100L);
    }

    /**
     * 쿠폰의 사용 수량 조회 API 성능 테스트
     * 여러 스레드를 통해 다양한 쿠폰 ID에 대한 사용 수량 조회 요청을 동시에 수행하며,
     * 평균 응답 시간이 100ms 이하인지 검증함
     */
    @Test
    void 쿠폰의_사용_수량_조회() throws InterruptedException {
        AtomicBoolean running = new AtomicBoolean(false);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicLong totalElapsedTime = new AtomicLong(0);

        int statusCode = RestAssured.get("/coupons/" + ThreadLocalRandom.current()
                .nextLong(MIN_COUPON_ID, MAX_COUPON_ID + 1) + "/used-count").statusCode();
        assertThat(statusCode).withFailMessage("쿠폰의 사용 수량 조회 API 호출에 실패했습니다. 테스트 대상 서버가 실행중인지 확인해 주세요.").isEqualTo(200);

        executeMultipleRequests(running, requestCount, totalElapsedTime,
                () -> RestAssured.get("/coupons/" + ThreadLocalRandom.current()
                        .nextLong(MIN_COUPON_ID, MAX_COUPON_ID + 1) + "/used-count"));

        System.out.println("Total request count: " + requestCount.get());
        System.out.println("Total elapsed time: " + totalElapsedTime.get() + "ms");

        long averageElapsedTime = totalElapsedTime.get() / requestCount.get();
        System.out.println("Average elapsed time: " + averageElapsedTime + "ms");

        assertThat(averageElapsedTime).isLessThanOrEqualTo(100L);
    }

    /**
     * 현재 발급 가능한 쿠폰 목록 조회 API 성능 테스트
     * 병렬 요청을 통해 발급 가능 쿠폰 조회 응답 평균 시간이 500ms 이하인지 검증
     */
    @Test
    void 현재_발급_가능한_쿠폰_조회() throws InterruptedException {
        AtomicBoolean running = new AtomicBoolean(false);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicLong totalElapsedTime = new AtomicLong(0);

        int statusCode = RestAssured.get("/coupons/issuable").statusCode();
        assertThat(statusCode).withFailMessage("발급 가능한 쿠폰 조회 API 호출에 실패했습니다. 테스트 대상 서버가 실행중인지 확인해 주세요.").isEqualTo(200);

        executeMultipleRequests(running, requestCount, totalElapsedTime, () -> RestAssured.get("/coupons/issuable"));

        System.out.println("Total request count: " + requestCount.get());
        System.out.println("Total elapsed time: " + totalElapsedTime.get() + "ms");

        long averageElapsedTime = totalElapsedTime.get() / requestCount.get();
        System.out.println("Average elapsed time: " + averageElapsedTime + "ms");

        assertThat(averageElapsedTime).isLessThanOrEqualTo(500L);
    }

    /**
     * 회원이 가지고 있는 사용 가능한 쿠폰 조회 API 성능 테스트
     * 무작위 회원 ID에 대해 병렬로 쿠폰 조회를 수행하고 평균 응답 시간이 100ms 이하인지 확인
     */
    @Test
    void 회원이_가지고_있는_사용_가능한_쿠폰_조회() throws InterruptedException {
        AtomicBoolean running = new AtomicBoolean(false);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicLong totalElapsedTime = new AtomicLong(0);

        int statusCode = RestAssured.get("/member-coupons/by-member-id?memberId=" + ThreadLocalRandom.current()
                .nextLong(MIN_MEMBER_ID, MAX_MEMBER_ID + 1)).statusCode();
        assertThat(statusCode).withFailMessage("회원이 가지고 있는 쿠폰 조회 API 호출에 실패했습니다. 테스트 대상 서버가 실행중인지 확인해 주세요.").isEqualTo(200);

        executeMultipleRequests(running, requestCount, totalElapsedTime,
                () -> RestAssured.get("/member-coupons/by-member-id?memberId=" + ThreadLocalRandom.current()
                        .nextLong(MIN_MEMBER_ID, MAX_MEMBER_ID + 1)));

        System.out.println("Total request count: " + requestCount.get());
        System.out.println("Total elapsed time: " + totalElapsedTime.get() + "ms");

        long averageElapsedTime = totalElapsedTime.get() / requestCount.get();
        System.out.println("Average elapsed time: " + averageElapsedTime + "ms");

        assertThat(averageElapsedTime).isLessThanOrEqualTo(100L);
    }

    /**
     * 2019년 1월부터 5월까지 월별로 쿠폰 할인을 가장 많이 받은 회원 조회 API 성능 테스트
     * 주어진 기간 내 임의의 월에 대해 병렬 요청을 보내고 평균 응답 시간이 100ms 이하인지 평가
     */
    @Test
    void 월별_쿠폰_할인을_가장_많이_받은_회원_조회() throws InterruptedException {
        AtomicBoolean running = new AtomicBoolean(false);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicLong totalElapsedTime = new AtomicLong(0);

        int statusCode = RestAssured.get("/marketing/max-coupon-discount-member?year=2019&month=1").statusCode();
        assertThat(statusCode).withFailMessage("월별 쿠폰 할인을 가장 많이 받은 회원 조회 API 호출에 실패했습니다. 테스트 대상 서버가 실행중인지 확인해 주세요.").isEqualTo(200);

        executeMultipleRequests(running, requestCount, totalElapsedTime, () -> {
            RestAssured.get(
                    "/marketing/max-coupon-discount-member?year=2019&month=" + ThreadLocalRandom.current().nextInt(1, 6));
        });

        System.out.println("Total request count: " + requestCount.get());
        System.out.println("Total elapsed time: " + totalElapsedTime.get() + "ms");

        long averageElapsedTime = totalElapsedTime.get() / requestCount.get();
        System.out.println("Average elapsed time: " + averageElapsedTime + "ms");

        assertThat(averageElapsedTime).isLessThanOrEqualTo(100L);
    }

    /**
     * 병렬로 여러 스레드를 사용해 지정된 Runnable 작업을 일정 시간 동안 반복 실행하는 메서드
     * @param running 작업 실행 상태를 제어하는 AtomicBoolean
     * @param requestCount 수행된 요청의 총 개수를 저장하는 AtomicInteger
     * @param totalElapsedTime 누적 처리 시간을 기록하는 AtomicLong
     * @param runnable 실행할 HTTP 요청 작업
     * @throws InterruptedException
     */
    private void executeMultipleRequests(AtomicBoolean running, AtomicInteger requestCount, AtomicLong totalElapsedTime,
                                         Runnable runnable)
            throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> executeRequest(running, requestCount, totalElapsedTime, runnable));
        }

        Thread.sleep(MILLISECONDS_IN_SECOND);    // 스레드 시작 후 1초 지연 후 실행 시작
        running.set(true);
        Thread.sleep(TEST_DURATION_SECONDS * MILLISECONDS_IN_SECOND);
        running.set(false);

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    /**
     * 단일 스레드에서 while 루프를 통해 지정된 작업을 실행하며, 실행 시간과 요청 건수를 기록함
     * @param running 작업 계속 실행 여부를 판단하는 플래그
     * @param requestCount 실행된 요청 수를 증가시키는 AtomicInteger
     * @param totalElapsedTime 개별 스레드 실행 시간 집계용 AtomicLong
     * @param runnable 실제 HTTP 요청을 담은 작업
     */
    private void executeRequest(AtomicBoolean running, AtomicInteger requestCount, AtomicLong totalElapsedTime,
                                Runnable runnable) {
        while (!running.get()) {
            // 테스트 시작 신호를 기다림
        }

        long elapsedTime = 0;
        while (running.get()) {
            long startTime = System.currentTimeMillis();
            runnable.run();
            long endTime = System.currentTimeMillis();

            elapsedTime += endTime - startTime;
            requestCount.incrementAndGet();
        }

        totalElapsedTime.addAndGet(elapsedTime);
    }

}
