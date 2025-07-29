package com.wowraid.jobspooncrawler.jumpit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowraid.jobspooncrawler.jumpit.entity.JumpitPositionFeatureDto;
import com.wowraid.jobspooncrawler.utility.RetryableRequestExecutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JumpitPositionsFeatureCrawler {

    public JumpitPositionFeatureDto crawl(String url) throws IOException {
        // HTML 문서 파싱을 CrawlerApiExecutor로 감싸서 실패 시 재시도
        Document doc = RetryableRequestExecutor.executeWithRetry(() -> {
            try {
                return parseHtmlDocument(url);
            } catch (IOException e) {
                throw new RuntimeException("HTML 파싱 실패", e); // Supplier는 체크 예외 처리 불가
            }
        });

        if (doc == null) {
            throw new IOException("크롤링 실패: Document를 가져오지 못함");
        }

        String title = extractTitle(doc);
        String company = extractCompany(doc);
        String deadline = extractDeadline(doc);
        List<String> techStacks = extractTechStacks(doc);
        String duties = extractPreBlock(doc, "주요업무");
        String requirements = extractPreBlock(doc, "자격요건");
        String preferred = extractPreBlock(doc, "우대사항");
        String benefits = extractPreBlock(doc, "복지 및 혜택");
        String location = extractLocation(doc);

        if (isBlank(title) || isBlank(company) || isBlank(deadline) || techStacks == null || techStacks.isEmpty()) {
            JsonNode fallback = extractFallbackJson(doc);
            if (fallback != null) {
                title = isBlank(title) ? fallback.path("title").asText(null) : title;
                company = isBlank(company) ? fallback.path("company").path("name").asText(null) : company;
                deadline = isBlank(deadline) ? fallback.path("closedAt").asText(null) : deadline;
                if (techStacks == null || techStacks.isEmpty()) {
                    techStacks = fallback.path("techStacks").findValuesAsText("name");
                }
            }
        }

        return buildFeatureDto(title, company, techStacks, duties, requirements, preferred, benefits, deadline, location);
    }

    private Document parseHtmlDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; JumpitCrawler/1.0)")
                .timeout(10000)
                .get();
    }

    private String extractTitle(Document doc) {
        return text(doc, "h1");
    }

    private String extractCompany(Document doc) {
        return text(doc, ".name span, a.name span");
    }

    private String extractDeadline(Document doc) {
        return text(doc, "dl:has(dt:containsOwn(마감일)) dd");
    }

    private String extractLocation(Document doc) {
        return text(doc, "dl:has(dt:containsOwn(근무지역)) dd li");
    }

    private List<String> extractTechStacks(Document doc) {
        return texts(doc, "dt:containsOwn(기술스택) + dd pre div");
    }

    private String extractPreBlock(Document doc, String label) {
        Element pre = doc.selectFirst("dt:containsOwn(" + label + ") + dd pre");
        return pre != null ? pre.text() : null;
    }

    private List<String> texts(Document doc, String selector) {
        return doc.select(selector).eachText();
    }

    private String text(Document doc, String selector) {
        Element el = doc.selectFirst(selector);
        return el != null ? el.text() : null;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private JsonNode extractFallbackJson(Document doc) {
        Elements scripts = doc.select("script");
        Pattern p = Pattern.compile("\\{\\s*\\\"queryKey\\\"\\s*:\\s*\\[\\\"position\\\",\\\"view\\\".*?\\}\\s*\\]\\s*\\}", Pattern.DOTALL);
        ObjectMapper mapper = new ObjectMapper();
        for (Element s : scripts) {
            String data = s.data();
            Matcher m = p.matcher(data);
            if (m.find()) {
                String jsonBlock = m.group();
                try {
                    JsonNode node = mapper.readTree(jsonBlock);
                    return findPositionNode(node);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private JsonNode findPositionNode(JsonNode node) {
        if (node == null) return null;
        if (node.has("title") && node.has("closedAt")) return node;
        if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode found = findPositionNode(child);
                if (found != null) return found;
            }
        } else if (node.isObject()) {
            var it = node.fields();
            while (it.hasNext()) {
                JsonNode found = findPositionNode(it.next().getValue());
                if (found != null) return found;
            }
        }
        return null;
    }

    private JumpitPositionFeatureDto buildFeatureDto(
            String title, String company, List<String> techStacks,
            String duties, String requirements, String preferred,
            String benefits, String deadline, String location
    ) {
        return JumpitPositionFeatureDto.builder()
                .title(title)
                .company(company)
                .techStacks(techStacks)
                .duties(duties)
                .requirements(requirements)
                .preferred(preferred)
                .benefits(benefits)
                .deadline(deadline)
                .location(location)
                .build();
    }
}
