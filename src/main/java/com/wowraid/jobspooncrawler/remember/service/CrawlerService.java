package com.wowraid.jobspooncrawler.remember.service;

import com.wowraid.jobspooncrawler.remember.entity.JobPosting;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Slf4j
@Service
public class CrawlerService {

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