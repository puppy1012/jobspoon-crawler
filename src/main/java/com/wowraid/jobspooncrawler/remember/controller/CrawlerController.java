package com.wowraid.jobspooncrawler.remember.controller;

import com.wowraid.jobspooncrawler.remember.entity.JobPosting;
import com.wowraid.jobspooncrawler.remember.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CrawlerController {
    @Autowired
    private CrawlerService crawlerService;
//    @PostMapping("/crawl")
//    public List<JobPosting> crawl(@RequestBody String filterJson) throws InterruptedException {
//        return crawlerService.crawl(filterJson);
    //}
    @GetMapping("test")
    public void test() throws InterruptedException {
        crawlerService.openChromeWindow();
    }
}
