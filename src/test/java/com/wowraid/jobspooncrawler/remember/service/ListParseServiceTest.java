package com.wowraid.jobspooncrawler.remember.service;

import com.wowraid.jobspooncrawler.remember.config.RememberProperties;
import com.wowraid.jobspooncrawler.remember.dto.JobListingDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * ListParseService 단위 테스트.
 * - RememberProperties.getListSelector()로 주입된 CSS 셀렉터가 실제로 적용되는지
 * - 제외 조건(:not([href*="web_high_salary_position"]))이 동작하는지
 * - 매칭 결과가 없을 때 빈 리스트를 반환하는지
 */
@ExtendWith(MockitoExtension.class)
class ListParseServiceTest {

    @Mock
    RememberProperties rememberProperties;

    @InjectMocks
    ListParseService listParseService;

    private static final String SELECTOR =
            "li > div > a[rel=\"noopener noreferrer\"]:not([href*=\"web_high_salary_position\"])";

    @Test
    @DisplayName("given: HTML에 3개의 a 태그(그중 1개는 제외 대상) when: parse 호출 then: 제외 대상은 제외되고 2개만 파싱된다")
    void parse_excludesHighSalaryPosition_andParsesOthers() {
        // given
        when(rememberProperties.getListSelector()).thenReturn(SELECTOR);

        // 매칭 대상 2개 (href에 web_high_salary_position 없음), 제외 대상 1개 (href에 포함)
        String html = """
                <ul>
                  <li>
                    <div>
                      <a rel="noopener noreferrer" href="/job/posting/1001">백엔드 개발자</a>
                    </div>
                  </li>
                  <li>
                    <div>
                      <a rel="noopener noreferrer" href="/job/posting/1002?source=web_high_salary_position">프론트엔드 개발자(광고)</a>
                    </div>
                  </li>
                  <li>
                    <div>
                      <a rel="noopener noreferrer" href="/job/posting/1003?isHighlight=false">iOS 개발자</a>
                    </div>
                  </li>
                </ul>
                """;

        // when
        List<JobListingDto> results = listParseService.parse(html);

        // then
        assertThat(results).hasSize(2);
        // 제목/링크가 정상적으로 추출됐는지 검증
        assertThat(results.get(0).getTitle()).isEqualTo("백엔드 개발자");
        assertThat(results.get(0).getDetailurl()).isEqualTo("/job/posting/1001");

        assertThat(results.get(1).getTitle()).isEqualTo("iOS 개발자");
        assertThat(results.get(1).getDetailurl()).isEqualTo("/job/posting/1003?isHighlight=false");
    }

    @Test
    @DisplayName("given: 셀렉터가 매우 제한적이어서 매칭이 없을 때 when: parse 호출 then: 빈 리스트 반환")
    void parse_returnsEmptyList_whenNoMatches() {
        // given
        // 의도적으로 존재하지 않을 법한 셀렉터로 주입
        when(rememberProperties.getListSelector()).thenReturn("li > div > a.foo-bar-baz-only");

        String html = """
                <ul>
                  <li><div><a rel="noopener noreferrer" href="/job/posting/2001">데이터 엔지니어</a></div></li>
                </ul>
                """;

        // when
        List<JobListingDto> results = listParseService.parse(html);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("given: 비어있거나 의미 없는 HTML when: parse 호출 then: 빈 리스트 반환")
    void parse_handlesEmptyOrGarbageHtml() {
        // given
        when(rememberProperties.getListSelector()).thenReturn(SELECTOR);

        // when
        List<JobListingDto> r1 = listParseService.parse("");
        List<JobListingDto> r2 = listParseService.parse("<html></html>");

        // then
        assertThat(r1).isEmpty();
        assertThat(r2).isEmpty();
    }
}
