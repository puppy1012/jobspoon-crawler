package com.wowraid.jobspooncrawler.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
class CrawlerApiExecutorTest {


    @Test
    @DisplayName("3번 재 시도후 실패를 return처리 확인")
    void executeWithRetry() {
        AtomicInteger attemptCounter = new AtomicInteger();
        String result = CrawlerApiExecutor.executeWithRetry(() -> {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("강제 실패");
        });

        assertThat(result).isNull();
        assertThat(attemptCounter.get()).isEqualTo(3); // 3번 재시도한 후 실패
    }
}