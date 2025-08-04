package com.wowraid.jobspooncrawler.remember.service;


import com.wowraid.jobspooncrawler.remember.dto.JobListingDto;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CrawlerService의 주요 메서드(fetchPageSource, fetchPageTitle, fetchDocument)를
 * 데이터 URL 기반의 간단한 HTML로 검증하는 통합 테스트 클래스입니다.
 *
 * - Base64 data URL 방식을 사용하여 공백, 특수문자 인코딩 문제를 해결합니다.
 * - Given-When-Then 형식으로 각 테스트의 의도를 명확히 주석 처리합니다.
 */
class CrawlerServiceTest {

    // Then: crawlerService 인스턴스를 생성하여 테스트 준비 완료
    private static CrawlerService crawlerService;

    /**
     * 테스트 전체에서 공유할 CrawlerService 인스턴스를 초기화합니다.
     * WebDriverManager를 이용해 ChromeDriver 바이너리를 자동으로 설치/설정하도록 합니다.
     */
    @BeforeAll
    static void setupClass() {
        // Given: 아직 ChromeDriver가 설정되지 않은 상태
        // When: WebDriverManager를 통해 크롬 드라이버를 자동으로 다운로드 및 경로 설정
        WebDriverManager.chromedriver().setup();
    }

    /**
     * 주어진 HTML 문자열을 Base64로 인코딩하여
     * data:text/html;base64,<base64> 형태의 data URL을 생성합니다.
     *
     * @param html 원본 HTML 문자열
     * @return Base64 인코딩된 data URL
     */
    private static String makeDataUrl(String html) {
        // Given: 인코딩할 HTML 문자열
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        // When: Base64로 인코딩 수행
        String base64 = Base64.getEncoder().encodeToString(bytes);
        // Then: Base64 스킴을 사용한 data URL 반환
        return "data:text/html;base64," + base64;
    }

    /**
     * fetchPageSource()가 페이지의 전체 HTML을 정확히 반환하는지 검증합니다.
     * Given-When-Then 구조로 테스트 흐름을 나눴습니다.
     */
    @Test
    void fetchPageSource_shouldContainFullHtml() {
        // Given: 간단한 HTML 문자열과 이를 Base64 data URL로 만든 URL
        String html = "<html><head><title>Test Title</title></head>"
                + "<body><h1>Hello</h1></body></html>";
        String dataUrl = makeDataUrl(html);

        // When: CrawlerService를 통해 페이지 소스(HTML 전체)를 가져옴
        String pageSource = crawlerService.fetchPageSource(dataUrl);

        // Then: 반환된 HTML 전체에 <title> 및 <h1> 요소가 포함되어야 함
        assertNotNull(pageSource, "fetchPageSource()는 null을 반환하면 안 됩니다.");
        assertTrue(
                pageSource.contains("<title>Test Title</title>"),
                "반환된 HTML에 <title>Test Title</title>이 포함되어야 합니다."
        );
        assertTrue(
                pageSource.contains("<h1>Hello</h1>"),
                "반환된 HTML에 <h1>Hello</h1>이 포함되어야 합니다."
        );
    }

    /**
     * fetchPageTitle()이 페이지의 <title> 요소만 정확히 추출하여 반환하는지 검증합니다.
     * HTML 전체가 아닌, title() 호출을 통해서만 결과를 얻도록 역할을 분리했음을 전제로 합니다.
     */
    @Test
    void fetchPageTitle_shouldReturnOnlyTitle() {
        // Given: 테스트용 HTML 문자열과 Base64 data URL
        String html = "<html><head><title>My Title</title></head>"
                + "<body>…</body></html>";
        String dataUrl = makeDataUrl(html);

        // When: CrawlerService의 fetchPageTitle() 메서드를 호출
        String title = crawlerService.fetchPageTitle(dataUrl);

        // Then: 반환된 문자열이 정확히 "My Title"이어야 함
        assertEquals(
                "My Title",
                title,
                "fetchPageTitle()는 페이지의 <title> 값을 정확히 반환해야 합니다."
        );
    }

    /**
     * fetchDocument()가 Jsoup을 이용해 HTML을 파싱하여 Document 객체를 생성하는지 확인합니다.
     * 파싱된 Document로부터 title과 특정 요소 텍스트를 검증합니다.
     */
    @Test
    void fetchDocument_shouldParseJsoupDocument() {
        // Given: id="para"를 가진 <p> 요소가 포함된 HTML과 Base64 data URL
        String html = "<html><head><title>Doc Title</title></head>"
                + "<body><p id=\"para\">Paragraph</p></body></html>";
        String dataUrl = makeDataUrl(html);

        // When: CrawlerService의 fetchDocument() 호출하여 Jsoup Document 획득
        Document doc = crawlerService.fetchDocument(dataUrl);

        // Then: Document가 null이 아니고, title과 p#para 텍스트가 예상대로 나와야 함
        assertNotNull(doc, "fetchDocument() 결과 Document는 null이 아니어야 합니다.");
        assertEquals(
                "Doc Title",
                doc.title(),
                "Jsoup으로 파싱한 title이 'Doc Title'이어야 합니다."
        );
        assertEquals(
                "Paragraph",
                doc.getElementById("para").text(),
                "id가 'para'인 요소의 텍스트가 'Paragraph'이어야 합니다."
        );
    }

    @Test
    @DisplayName("parseLiElements: 정상 HTML에서 <li> → <div> → <a> 추출")
    void parseLiElements() {
        // given: 두 개의 <li> 요소가 있는 샘플 HTML
        String html = """
            <ul>
              <li><div><a href="/job1">Job 1</a></div></li>
              <li><div><a href="/job2">Job 2</a></div></li>
            </ul>
            """;

        // when: 파싱 메서드 호출
        List<JobListingDto> list = crawlerService.parseLiElements(html);

        // then: 두 개가 정확히 파싱되고, title/href 값이 일치해야 함
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getTitle()).isEqualTo("Job 1");
        assertThat(list.get(0).getDetailurl()).isEqualTo("/job1");
        assertThat(list.get(1).getTitle()).isEqualTo("Job 2");
        assertThat(list.get(1).getDetailurl()).isEqualTo("/job2");
    }
}
