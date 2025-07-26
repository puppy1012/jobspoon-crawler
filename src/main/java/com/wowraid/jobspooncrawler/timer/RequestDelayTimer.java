package com.wowraid.jobspooncrawler.timer;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class RequestDelayTimer {
    private static final Random random = new Random();

    // 인스턴스 생성을 막기 위한 private 생성자
    private RequestDelayTimer() {}

    // 요청 간 지연을 처리하는 정적 유틸 메서드
    public static void delay() {
        int delayMillis = random.nextInt(501) + 1000; //
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("딜레이 중 인터럽트 발생", e);
        }
    }
}
