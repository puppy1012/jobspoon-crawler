package com.wowraid.jobspooncrawler.remember.keyword;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class RememberKeywordService {

    // .env 에서 쉼표로 구분된 키워드 목록을 읽어와 List<String> 으로 변환
    @Value("#{'${SW_KEYWORDS}'.split(',')}")
    private List<String> swKeywords;

    @Value("#{'${AI_KEYWORDS}'.split(',')}")
    private List<String> aiKeywords;

    /**
     * level1 카테고리 이름("SW개발" or "AI·데이터") 을 받아
     * 해당하는 level2 리스트로 JSON 문자열을 조립한 뒤 URL-encode 처리
     */
    public String toQueryString(String level1,List<String>level2list) {
        StringBuilder json = new StringBuilder("{\"jobCategoryNames\":[");
        for (int i = 0; i < level2list.size(); i++) {
            String level2 = level2list.get(i);
            json.append(String.format(
                    "{\"level1\":\"%s\",\"level2\":\"%s\"}", level1, level2
            ));
            if (i < level2list.size() - 1) {
                json.append(",");
            }
        }
        json.append("]}");
        log.info(json.toString());
        return URLEncoder.encode(json.toString(), StandardCharsets.UTF_8);

    }
    public List<String> getLevel2keywords(String level1){
        switch (level1) {
            case "SW개발": return swKeywords;
            case "AI·데이터": return aiKeywords;
            default:    return List.of();
        }
    }
}
