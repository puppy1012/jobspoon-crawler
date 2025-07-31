package com.wowraid.jobspooncrawler.remember.controller;

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
    @GetMapping("/test_url")
    public void testUrl() throws InterruptedException {
        crawlerService.openChromeWindow();
    }
    // HTTP GET 요청을 '/test_url2' 엔드포인트에 매핑
    @GetMapping("/test_url2")
    public Mono<String> testUrl2() {// 쿼리 파라미터 'url'을 메서드 파라미터로 바인딩
        // 1) Mono.fromCallable: Blocking 메서드(fetchPageSource)를 호출 가능한 형태로 래핑
        //    호출 시점은 구독(subscribe)될 때
        return Mono.fromCallable(() ->
                        // 2) 실제로 URL 페이지 소스를 가져오는 Blocking 호출
                        crawlerService.fetchPageSource(url)
                )
                // 3) 이 Callable이 실행될 스레드 풀 지정
                //    boundedElastic: I/O·블로킹 작업 최적화 스레드 풀
                .subscribeOn(Schedulers.boundedElastic())

                // 4) 파이프라인에서 예외 발생 시 대체 흐름 정의
                .onErrorResume(ex -> {
                    // 4-1) 로깅: 어떤 예외가 터졌는지 스택트레이스와 함께 기록
                    log.error("Error in testUrl2", ex);
                    // 4-2) 에러 복구: 에러 메시지를 담은 단일 Mono<String> 반환
                    return Mono.just("Error fetching URL: " + ex.getMessage());
                });
    }

}
