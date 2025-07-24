package com.wowraid.jobspooncrawler.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowraid.jobspooncrawler.entity.JumpitPositionListDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.parser.Parser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class JumpitPositionsListCrawler {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<JumpitPositionListDto> crawl(String url) throws IOException {
        log.info("receive crawlingUrl: {}", url);

        // 1) API가 JSON을 반환하므로 Jsoup 대신 JSON 파싱
        String json = restTemplate.getForObject(url, String.class);
        log.info("receive json: {}", json);

        // 2) Jackson으로 파싱
        JsonNode root = objectMapper.readTree(json);
        JsonNode positions = root.path("result").path("positions");

        List<JumpitPositionListDto> listResult = new ArrayList<>();
        for (JsonNode pos : positions) {
            String id       = pos.path("id").asText();
            String closedAt = pos.path("closedAt").asText();
            log.info("id: {} closedAt: {}", id, closedAt);

            listResult.add(new JumpitPositionListDto(id, closedAt));
        }

        return listResult;
    }

}
