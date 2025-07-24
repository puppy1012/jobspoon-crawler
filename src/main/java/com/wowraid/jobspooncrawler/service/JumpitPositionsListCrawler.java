package com.wowraid.jobspooncrawler.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowraid.jobspooncrawler.entity.JumpitPositionListDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.RateLimiter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class JumpitPositionsListCrawler {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<JumpitPositionListDto> crawl(String url) throws IOException {
        Random ran=new Random();
        int ranNum=ran.nextInt(301)+300;
        List<JumpitPositionListDto> listResult = new ArrayList<>();
        int pageid = 1;
        while (true) {
            List<JumpitPositionListDto> list = getJumpitPositionList(url, pageid);
            if (list.isEmpty()) {
                break;
            }
            listResult.addAll(list);
            pageid++;
            try {
                Thread.sleep(ranNum);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 권장: 인터럽트 상태를 복구
                log.warn("크롤링 중 인터럽트 발생", e);
//                break; // 크롤링을 종료할 수도 있음
            }
        }
        return listResult;
    }


    private List<JumpitPositionListDto> getJumpitPositionList(String url, int pageid) throws IOException {
        String targeturl = String.format(url, pageid);
        // 1) API가 JSON을 반환하므로 Jsoup 대신 JSON 파싱
        String json = restTemplate.getForObject(targeturl, String.class);
        log.info("receive json: {}", json);

        // 2) Jackson으로 파싱
        JsonNode root = objectMapper.readTree(json);
        JsonNode positions = root.path("result").path("positions");
        List<JumpitPositionListDto> jumpitPositionListDto = new ArrayList<JumpitPositionListDto>();

        for (JsonNode pos : positions) {
            String id = pos.path("id").asText();
            String closedAt = pos.path("closedAt").asText();
            log.info("id: {} closedAt: {}", id, closedAt);
            jumpitPositionListDto.add(new JumpitPositionListDto(id, closedAt));
        }
        return jumpitPositionListDto;
    }
}
