
package com.wowraid.jobspooncrawler.controller;

import com.wowraid.jobspooncrawler.domain.JobPost;
import com.wowraid.jobspooncrawler.service.JumpitCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
public class CrawlController {

    private final JumpitCrawler crawler;

    public CrawlController(JumpitCrawler crawler) {
        this.crawler = crawler;
    }

    @GetMapping("/crawl")
    public JobPost crawl(@RequestParam String url) throws Exception {
        JobPost crawlresult = crawler.crawl(url);
        log.info(crawlresult.toString());
        return crawlresult;
    }
}
