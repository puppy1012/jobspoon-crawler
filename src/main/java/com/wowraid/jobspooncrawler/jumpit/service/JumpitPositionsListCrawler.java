package com.wowraid.jobspooncrawler.jumpit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowraid.jobspooncrawler.jumpit.entity.JumpitPositionListDto;
import com.wowraid.jobspooncrawler.utility.RetryableRequestExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JumpitPositionsListCrawler {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<JumpitPositionListDto> crawl(String url) throws IOException {
        List<JumpitPositionListDto> listResult = new ArrayList<>();
        int pageid = 1;
        while (true) {

            int finalPageid = pageid;
            List<JumpitPositionListDto> list = RetryableRequestExecutor.executeWithRetry(() ->
                    getJumpitPositionList(url, finalPageid)
            );
            if (list == null || list.isEmpty()) break;

            log.info("list result= "+list.toString());
            log.info("pageid= "+pageid);
            listResult.addAll(list);
            if(pageid==3){break;}//for test
            pageid++;


        }
        return listResult;
    }


    private List<JumpitPositionListDto> getJumpitPositionList(String url, int pageid) {
        try {
            String targeturl = String.format(url, pageid);
            String json = restTemplate.getForObject(targeturl, String.class);
            JsonNode root = objectMapper.readTree(json);
            JsonNode positions = root.path("result").path("positions");

            List<JumpitPositionListDto> result = new ArrayList<>();
            for (JsonNode pos : positions) {
                result.add(new JumpitPositionListDto(pos.path("id").asText(), pos.path("closedAt").asText()));
            }
            return result;

        } catch (IOException e) {
            throw new RuntimeException("JSON 파싱 실패", e); // 감싸서 던지기
        }
    }

}
