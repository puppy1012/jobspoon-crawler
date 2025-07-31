package com.wowraid.jobspooncrawler.remember.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CrawlerService {

    private static final String USER_AGENT = "'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36'";

    /**
     * 지정된 URL을 ChromeDriver를 통해 가져온 뒤, HTML을 반환합니다.
     */
    public String fetchPageSource(String url) {
        // 1) 메서드 진입 로그
        log.info("[fetchPageSource] Start. URL={}", url);

        // 2) ChromeDriver 자동 설치
        WebDriverManager.chromedriver().setup();
        log.info("[fetchPageSource] ChromeDriver setup complete");

        // 3) ChromeOptions 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-agent=" + USER_AGENT);
        // 필요 시 헤드리스 모드 추가 가능
        options.addArguments("--headless=new");
        log.info("[fetchPageSource] ChromeOptions configured (headless={}, UA={})",
                options.toString().contains("--headless"), USER_AGENT);

        // 4) 드라이버 실행
        WebDriver driver = new ChromeDriver(options);
        log.info("[fetchPageSource] ChromeDriver session started");

        try {
            // 5) 페이지 이동
            log.info("[fetchPageSource] Navigating to URL");
            driver.get(url);
            log.info("[fetchPageSource] Page loaded: {}", driver.getCurrentUrl());

            // 6) JS 로딩 대기
            log.info("[fetchPageSource] Waiting 2 seconds for JS to load");
            Thread.sleep(2000);

            // 7) 페이지 소스 및 제목 가져오기
            String pageSource = driver.getPageSource();
            String pageTitle  = driver.getTitle();
            log.info("[fetchPageSource] Page fetched. title=\"{}\", sourceLength={}",
                    pageTitle, pageSource.length());

            return pageSource;
        } catch (InterruptedException e) {
            // 8) 인터럽트 예외 처리
            Thread.currentThread().interrupt();
            log.error("[fetchPageSource] Interrupted while sleeping", e);
            return "";
        } finally {
            // 9) 리소스 정리
            driver.quit();
            log.info("[fetchPageSource] ChromeDriver session closed");
        }
    }
    /**
     * 페이지의 <title>을 가져옵니다.
     */
    public String fetchPageTitle(String url) {
        Document doc = fetchDocument(url);
        return doc.title();
    }

    /**
     * fetch()로 가져온 HTML 문자열을 Jsoup으로 파싱하여 Document를 반환합니다.
     */
    public Document fetchDocument(String url) {
        String html = fetchPageSource(url);
        return Jsoup.parse(html);
    }
    /**
     * Selenium Manager로 Chrome을 실행만 합니다.
     */
    public void openChromeWindow() throws InterruptedException {
        // 최소 옵션으로 Chrome 실행
        ChromeOptions options = new ChromeOptions();
        // 필요 시 옵션 추가 (예: 헤드리스 제거하여 실제 창 확인 가능)
        // options.addArguments("--headless=new");

        WebDriver driver = new ChromeDriver(options);
        log.info("Chrome 창이 열렸습니다.");

        // 테스트용 대기
        Thread.sleep(5000);

        driver.quit();
        log.info("Chrome 창이 종료되었습니다.");
    }
}
