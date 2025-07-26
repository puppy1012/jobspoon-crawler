package com.wowraid.jobspooncrawler.utility;

import com.wowraid.jobspooncrawler.timer.RequestDelayTimer;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class RetryableRequestExecutor {

    // 최대 재시도 횟수
    private static final int MAX_RETRIES = 3;
    private static final int BASE_DELAY_MS = 500;

    private RetryableRequestExecutor() {} // 인스턴스화 방지

    /**
     * 재시도 + 슬로틀링 포함한 요청 실행기
     *
     * @param task 요청 로직을 담은 Supplier
     * @param <T>  반환 타입
     * @return 결과값 또는 null
     */
    public static <T> T executeWithRetry(Supplier<T> task) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                T result = task.get();
                RequestDelayTimer.delay(); // 요청 간 딜레이
                return result;

            } catch (Exception e) {
                log.warn("크롤링 예외 발생 (시도 {} / {}): {}", attempt, MAX_RETRIES, e.getMessage());
                if (attempt == MAX_RETRIES) {
                    log.error("최대 재시도 횟수 초과, 크롤링 실패");
                    return null; // 실패 처리
                }

                try {
                    Thread.sleep(BASE_DELAY_MS * attempt); // 재시도 간 backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }
}