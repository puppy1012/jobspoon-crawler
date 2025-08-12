package com.wowraid.jobspooncrawler.remember.application.browser;

import io.github.bonigarcia.wdm.WebDriverManager;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimpleDriverProvider {
    /**
     * 싱글턴(Spring @Component 기본 스코프) 빈에서 공유할 WebDriver 인스턴스.
     * - 지연 초기화(lazy init): 최초 getDriver() 호출 시 생성.
     * - 동시성 제어: getDriver()/closeDriver()를 synchronized 로 보호하여
     *   멀티스레드 환경에서 중복 생성/종료를 방지.
     *   (크롤러가 멀티스레드로 확장될 가능성이 있으면 ThreadLocal 전략을 별도 검토 필요)
     */
    private WebDriver driver;

    /**
     * application.yml 등에 정의된 사용자 에이전트 값을 주입.
     * 예) application.yml
     *   crawler:
     *     chrome:
     *       user-agent: "Mozilla/5.0 (...) Chrome/138.0.0.0 Safari/537.36"
     *
     * 주의:
     * - 필드명이 대문자지만 상수가 아니라 @Value 주입되는 인스턴스 필드
     *   (관례상 소문자 카멜케이스 권장이나, 기능상 문제는 없음)
     */
    @Value("${crawler.chrome.user-agent}")
    private String USER_AGENT;

    /**
     * Chrome WebDriver를 획득(필요 시 생성)하는 팩토리 메서드.
     * - synchronized 로 한 번에 하나의 스레드만 접근 가능하도록 하여
     *   중복 생성 방지.
     * - 최초 호출 시:
     *    1) WebDriverManager 가 OS/아키텍처에 맞는 chromedriver 바이너리 확인/설치
     *    2) ChromeOptions 에 --user-agent 인자 주입
     *    3) ChromeDriver 인스턴스 생성 및 로그 남김
     * - 이후 호출 시: 이미 생성된 driver 반환.
     */
    public synchronized WebDriver getDriver() {
        if (driver == null) {
            // chromedriver 바이너리 자동 관리(버전 호환 포함).
            WebDriverManager.chromedriver().setup();

            // Chrome 실행 옵션 구성.
            ChromeOptions options = new ChromeOptions();

            // UA 스푸핑: 서버 측 UA 기반 차단 회피나 트래픽 세분화를 위해 사용.
            // 값에 공백/괄호가 포함되어도 Chrome은 하나의 인자로 인식함.
            options.addArguments("--user-agent=" + USER_AGENT);

            // (주의) 현재는 GUI 모드. 서버(헤드리스) 환경이면 다음 옵션 고려:
            // options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");

            // 실제 브라우저 프로세스를 띄우는 드라이버 생성.
            driver = new ChromeDriver(options);

            // 운영 추적용 로그(UA 확인 포함).
            log.info("[SimpleDriverProvider] Chrome driver started (UA={})", USER_AGENT);
        }
        return driver;
    }

    /**
     * 컨테이너 종료 시점(정상 셧다운) 콜백.
     * - Spring 컨텍스트가 내려갈 때 호출되어 브라우저/드라이버 프로세스를 정리.
     * - synchronized 로 종료 중 동시 접근을 방지.
     * - try 블록에서 예외가 나더라도 애플리케이션 종료 흐름을 막지 않도록 처리.
     *
     * 주의:
     * - 강제 종료(SIGKILL) 등 비정상 종료 시에는 @PreDestroy 가 실행되지 않을 수 있음.
     * - 현재 코드는 예외 발생 시에만 driver = null 로 초기화. 정상 종료 후에도
     *   레퍼런스 해제를 원하면 finally 에서 null 할당을 검토.
     */
    @PreDestroy
    public synchronized void closeDriver() {
        if (driver != null) {
            try {
                // 브라우저와 세션을 정상 종료(프로세스 포함).
                driver.quit();
            } catch (Exception e) {
                log.warn("[SimpleDriverProvider] Error closing chrome driver", e);
            }finally{
                driver = null;
                log.info("[SimpleDriverProvider] Chrome driver closed");
            }
        }
    }
}
