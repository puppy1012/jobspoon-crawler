package com.wowraid.jobspooncrawler.remember.controller;

import com.wowraid.jobspooncrawler.remember.dto.JobListingDto;
import com.wowraid.jobspooncrawler.remember.entity.JobPosting;
import com.wowraid.jobspooncrawler.remember.service.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api")
public class CrawlerController {
    @Autowired
    private CrawlerService crawlerService;
    private String url= "https://career.rememberapp.co.kr/job/postings";
//    @PostMapping("/crawl")
//    public List<JobPosting> crawl(@RequestBody String filterJson) throws InterruptedException {
//        return crawlerService.crawl(filterJson);
    //}
    @GetMapping("test")
    public void test() throws InterruptedException {
        crawlerService.openChromeWindow();
    }
    @GetMapping("/SeleniumTest")
    public void testUrl() throws InterruptedException {
        crawlerService.openChromeWindow();
    }
    // HTTP GET 요청을 '/url' 엔드포인트에 매핑
    @GetMapping("/url")
    public List<JobListingDto> RememberPostingUrl () {
        // 1) 메서드 진입 로그
        log.info("[RememberPostingUrl] Start. URL={}", url);
        try {
            // 2) Blocking 메서드 직접 호출
            List<JobListingDto> pageSource = crawlerService.fetchLiElements(url);
            // 3) 결과 반환
            return pageSource;
        } catch (Exception ex) {
            // 4) 예외 처리 및 복구 흐름
            log.error("[RememberPostingUrl] Error fetching URL", ex);
            return null;
        }
    }

}
