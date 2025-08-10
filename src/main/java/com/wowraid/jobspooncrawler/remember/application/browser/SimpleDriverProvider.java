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
    private WebDriver driver;
    @Value("${crawler.chrome.user-agent}")
    private String USER_AGENT;

    public synchronized WebDriver getDriver() {
        if (driver == null) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--user-agent="+USER_AGENT);
            driver = new ChromeDriver(options);
            log.info("[SimpleDriverProvider] Chrome driver started (UA={})", USER_AGENT);
        }
        return driver;
    }

    @PreDestroy
    public synchronized void closeDriver() {
        if (driver != null) {
            try{
                driver.quit();
            }catch (Exception e) {
                driver = null;
                log.info("[SimpleDriverProvider] Chrome driver closed");
            }
        }
    }
}
