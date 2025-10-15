package coupon;

import java.util.concurrent.Executor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
@Import(DataSourceJpaConfig.class)
public class CouponApplication implements AsyncConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(CouponApplication.class, args);
    }

    /**
     * couponTaskExecutor라는 이름으로 등록되는 TaskExecutor 빈 설정 메서드
     *
     * ThreadPoolTaskExecutor를 사용해 비동기 작업을 처리하기 위한 쓰레드 풀을 설정함
     * 주요 설정 내용:
     * - 쓰레드 이름 접두사 "coupon-" 지정으로 디버깅 시 쓰레드 구분 용이
     * - corePoolSize: 50 (기본 유지할 최소 쓰레드 수)
     * - maxPoolSize: 최대 Integer.MAX_VALUE (필요시 확장 가능한 최대 쓰레드 수)
     * - queueCapacity: Integer.MAX_VALUE (대기 큐 크기를 최대치로 설정)
     * - keepAliveSeconds: 60초간 비활성 쓰레드 유지 후 종료
     * - shutdown 시 대기 중인 태스크가 완료될 때까지 기다리도록 설정 (waitForTasksToCompleteOnShutdown)
     * - 종료 대기 시간 최대 60초 (awaitTerminationSeconds)
     * - 빈 이름은 "couponTaskExecutor"로 지정
     *
     * 이 설정은 대규모 병렬 비동기 작업 처리에 맞춘 쓰레드풀 환경을 구성함
     */
    @Bean(name = "couponTaskExecutor")
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("coupon-");
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(Integer.MAX_VALUE);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setBeanName("couponTaskExecutor");
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return executor();
    }
}
