
package com.wowraid.jobspooncrawler.jumpit.controller;

import com.wowraid.jobspooncrawler.jumpit.entity.JumpitPositionFeatureDto;
import com.wowraid.jobspooncrawler.jumpit.entity.JumpitPositionListDto;
import com.wowraid.jobspooncrawler.jumpit.service.JumpitPositionsFeatureCrawler;
import com.wowraid.jobspooncrawler.jumpit.service.JumpitPositionsListCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/crawl")
public class CrawlController {
    // 공통 베이스 URL + 파라미터로 page를 붙여서 사용
    private static final String POSITION_LIST_BASE_URL =
            "https://jumpit-api.saramin.co.kr/api/positions?sort=reg_dt&highlight=false&page=%d";

    private final JumpitPositionsFeatureCrawler featureCrawler;
    private final JumpitPositionsListCrawler listCrawler;

    public CrawlController(JumpitPositionsFeatureCrawler featureCrawler, JumpitPositionsListCrawler listCrawler) {
        this.featureCrawler = featureCrawler;
        this.listCrawler = listCrawler;
    }

    @GetMapping("/positions/feature")
    public JumpitPositionFeatureDto crawl(@RequestParam String url) throws Exception {
        log.info("crawling " + url);
        JumpitPositionFeatureDto crawlresult = featureCrawler.crawl(url);
        log.info(crawlresult.toString());
        return crawlresult;
    }
    @GetMapping("/positions/list")
    public List<JumpitPositionListDto> crawlList() throws Exception {
        log.info("Crawling list from URL={}", POSITION_LIST_BASE_URL);
        List<JumpitPositionListDto> results = listCrawler.crawl(POSITION_LIST_BASE_URL);
        log.info(results.toString());
        return results;
    }
}
