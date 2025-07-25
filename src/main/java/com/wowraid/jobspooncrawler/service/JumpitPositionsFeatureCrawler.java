package com.wowraid.jobspooncrawler.service;

import com.wowraid.jobspooncrawler.entity.JumpitPositionFeatureDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service // 스프링 서비스 컴포넌트로 등록.
public class JumpitPositionsFeatureCrawler {

    // 실제로는 파라미터로 받도록 만들고, 배치에선 여러 URL 돌리면 됨.
    public JumpitPositionFeatureDto crawl(String url) throws IOException {
        Document doc = Jsoup.connect(url) // URL에 HTTP 요청을 보냄.
                .userAgent("Mozilla/5.0 (compatible; JumpitCrawler/1.0)") // HTTP User-Agent 설정.
                .timeout(10000) // 타임아웃을 10초로 설정.
                .get(); // HTML 문서를 가져옴.

        String title = text(doc, "h1"); // 게시물 제목을 h1 태그에서 추출.
        String company = text(doc, ".name span, a.name span"); // 회사명을 지정된 CSS 셀렉터로 추출.

        List<String> techStacks = texts(doc, "dt:containsOwn(기술스택) + dd pre div"); // 기술 스택 리스트를 추출.

        String duties = preBlock(doc, "주요업무"); // 주요 업무 내용을 pre 태그 블록에서 추출.
        String requirements = preBlock(doc, "자격요건"); // 자격 요건을 pre 태그 블록에서 추출.
        String preferred = preBlock(doc, "우대사항"); // 우대 사항을 pre 태그 블록에서 추출.
        String benefits = preBlock(doc, "복지 및 혜택"); // 복지 및 혜택을 pre 태그 블록에서 추출.

        String deadline = text(doc, "dl:has(dt:containsOwn(마감일)) dd"); // 마감일을 지정된 셀렉터에서 추출.
        String location = text(doc, "dl:has(dt:containsOwn(근무지역)) dd li"); // 근무 지역을 li 태그에서 추출.

        // Fallback: script JSON
        if (isBlank(title) || isBlank(company) || isBlank(deadline)) { // 필수 정보가 비어있으면 JSON에서 보완.
            JsonNode root = extractJson(doc); // 페이지 내 스크립트에서 JSON 추출.
            if (root != null) { // JSON 노드가 존재하면 필드 보완 시도.
                if (isBlank(title))      title = root.path("title").asText(null); // title 보완.
                if (isBlank(company))    company = root.path("company").path("name").asText(null); // company 보완.
                if (isBlank(deadline))   deadline = root.path("closedAt").asText(null); // deadline 보완.
                if (techStacks == null || techStacks.isEmpty()) { // techStacks 비어있으면 배열에서 보완.
                    if (root.has("techStacks") && root.path("techStacks").isArray()) {
                        techStacks = root.path("techStacks") // 배열 요소의 name 값 리스트로 변환.
                                .findValuesAsText("name");
                    }
                }
            }
        }

        return JumpitPositionFeatureDto.builder() // JobPost 객체를 빌더 패턴으로 생성.
                .title(title)
                .company(company)
                .techStacks(techStacks)
                .duties(duties)
                .requirements(requirements)
                .preferred(preferred)
                .benefits(benefits)
                .deadline(deadline)
                .location(location)
                .build(); // 객체 반환.
    }

    private String preBlock(Document doc, String label) {
        Element pre = doc.selectFirst("dt:containsOwn(" + label + ") + dd pre"); // 레이블에 맞는 pre 요소 선택.
        return pre != null ? pre.text() : null; // 텍스트 반환 또는 null.
    }

    private List<String> texts(Document doc, String selector) {
        return doc.select(selector).eachText(); // 지정된 셀렉터의 모든 텍스트 리스트 반환.
    }

    private String text(Document doc, String selector) {
        Element el = doc.selectFirst(selector); // 첫 번째 매칭 요소 선택.
        return el != null ? el.text() : null; // 텍스트 반환 또는 null.
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank(); // 문자열이 null이거나 공백일 때 true 반환.
    }

    /**
     * Extract hydrated JSON embedded in <script> tags (React Query etc.).
     */
    private JsonNode extractJson(Document doc) {
        Elements scripts = doc.select("script"); // 모든 스크립트 태그 선택.
        Pattern p = Pattern.compile("\\{\\s*\"queryKey\"\\s*:\\s*\\[\"position\",\"view\".*?\\}\\s*\\]\\s*\\}", Pattern.DOTALL); // 패턴 정의.
        ObjectMapper mapper = new ObjectMapper(); // Jackson ObjectMapper 생성.
        for (Element s : scripts) { // 스크립트 태그 순회.
            String data = s.data(); // 내부 스크립트 데이터 추출.
            Matcher m = p.matcher(data); // 패턴 매처 생성.
            if (m.find()) { // 매칭되는 JSON 블록이 있으면.
                String jsonBlock = m.group(); // 매칭된 문자열 가져옴.
                try {
                    JsonNode node = mapper.readTree(jsonBlock); // JSON 파싱.
                    return findPositionNode(node); // 필요한 노드 탐색.
                } catch (Exception ignored) {} // 파싱 오류 무시.
            }
        }
        return null; // JSON을 찾지 못하면 null 반환.
    }

    /**
     * Recursively find node that contains title/company/closedAt keys.
     */
    private JsonNode findPositionNode(JsonNode node) {
        if (node == null) return null; // null 체크.
        if (node.has("title") && node.has("closedAt")) {
            return node; // 원하는 필드를 가진 경우 반환.
        }
        if (node.isArray()) {
            for (JsonNode child : node) { // 배열 요소 순회.
                JsonNode found = findPositionNode(child); // 재귀 탐색.
                if (found != null) return found;
            }
        } else if (node.isObject()) {
            var it = node.fields(); // 객체 필드 순회 이터레이터.
            while (it.hasNext()) {
                JsonNode found = findPositionNode(it.next().getValue()); // 값 재귀 탐색.
                if (found != null) return found;
            }
        }
        return null; // 찾지 못하면 null 반환.
    }

    /*
     * Summary of JumpitCrawler 동작 구조.
     * 1. crawl(url): URL에 HTTP 요청을 보내 HTML 문서를 가져옴.
     * 2. CSS 셀렉터 및 preBlock 메서드로 제목, 회사명, 기술 스택 등 주요 정보 추출.
     * 3. 필수 정보가 누락되면 extractJson로 페이지 내 스크립트 JSON에서 보완.
     * 4. 추출된 데이터를 JobPost DTO로 빌더 패턴을 사용해 반환.
     */
}
