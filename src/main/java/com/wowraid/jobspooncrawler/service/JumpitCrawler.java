
package com.wowraid.jobspooncrawler.service;

import com.wowraid.jobspooncrawler.domain.JobPost;
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

@Service
public class JumpitCrawler {

    // 실제로는 파라미터로 받도록 만들고, 배치에선 여러 URL 돌리면 됨.
    public JobPost crawl(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; JumpitCrawler/1.0)")
                .timeout(10000)
                .get();

        String title = text(doc, "h1");
        String company = text(doc, ".name span, a.name span");

        List<String> techStacks = texts(doc, "dt:containsOwn(기술스택) + dd pre div");

        String duties = preBlock(doc, "주요업무");
        String requirements = preBlock(doc, "자격요건");
        String preferred = preBlock(doc, "우대사항");
        String benefits = preBlock(doc, "복지 및 혜택");

        String deadline = text(doc, "dl:has(dt:containsOwn(마감일)) dd");
        String location = text(doc, "dl:has(dt:containsOwn(근무지역)) dd li");

        // Fallback: script JSON
        if (isBlank(title) || isBlank(company) || isBlank(deadline)) {
            JsonNode root = extractJson(doc);
            if (root != null) {
                if (isBlank(title))      title = root.path("title").asText(null);
                if (isBlank(company))    company = root.path("company").path("name").asText(null);
                if (isBlank(deadline))   deadline = root.path("closedAt").asText(null);
                if (techStacks == null || techStacks.isEmpty()) {
                    // techStacks is often array under "techStacks"
                    if (root.has("techStacks") && root.path("techStacks").isArray()) {
                        techStacks = root.path("techStacks")
                                .findValuesAsText("name");
                    }
                }
            }
        }

        return JobPost.builder()
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

    private String preBlock(Document doc, String label) {
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

    /** Extract hydrated JSON embedded in <script> tags (React Query etc.) */
    private JsonNode extractJson(Document doc) {
        Elements scripts = doc.select("script");
        Pattern p = Pattern.compile("\\{\\s*\"queryKey\"\\s*:\\s*\\[\"position\",\"view\".*?\\}\\s*\\]\\s*\\}", Pattern.DOTALL);
        ObjectMapper mapper = new ObjectMapper();
        for (Element s : scripts) {
            String data = s.data();
            Matcher m = p.matcher(data);
            if (m.find()) {
                String jsonBlock = m.group();
                try {
                    // Sometimes it's an array/object wrapper; try to find "state" or the last object that contains needed keys.
                    JsonNode node = mapper.readTree(jsonBlock);
                    // Heuristic: deep search for keys
                    return findPositionNode(node);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    /** Recursively find node that contains title/company/closedAt keys. */
    private JsonNode findPositionNode(JsonNode node) {
        if (node == null) return null;
        if (node.has("title") && node.has("closedAt")) {
            return node;
        }
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
}
