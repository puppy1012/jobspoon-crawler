package com.wowraid.jobspooncrawler.remember.service;

import com.wowraid.jobspooncrawler.remember.config.RememberProperties;
import com.wowraid.jobspooncrawler.remember.dto.JobListingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListParseService {

    private final RememberProperties rememberProperties;
    /**
     * HTML 문자열에서 모든 <li> 요소의 텍스트를 추출하는 메서드.
     */
    public List<JobListingDto> parse(String html) {
        // Jsoup 파서로 HTML 문서 객체 생성.
        Document doc = Jsoup.parse(html);
        // 모든 <li> 요소 선택.
        Elements items = doc.select(rememberProperties.getListSelector());
        // 요소별 텍스트를 저장할 리스트 생성.
        List<JobListingDto> results = new ArrayList<>();
        // 각 <li> 요소의 텍스트 추출 및 리스트에 추가.
        for (Element li : items) {
            String title=li.text();
            String href=li.attr("href");
            log.info("title= "+title+" href= "+href);
            results.add(new JobListingDto(title,href));
        }
        return results;
    }
}
