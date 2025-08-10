package com.wowraid.jobspooncrawler.remember.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter @Setter
@Validated
@ConfigurationProperties(prefix = "remember")
public class RememberProperties {

    //Remember 기본 url
    @Value("${crawler.remember.base-url}")
    private String Baseurl;

    //페이지 로딩 대기(ms)처리
    private long waitMillis=5000;

    //목록 파싱에 사용할 CSS 셀렉터
    private String listSelector=
            "li > div"
            + " > a[rel=\"noopener noreferrer\"]" //초기화등 버튼요소 제거용
            + ":not([href*=\"web_high_salary_position\"])"; //광고성 채용공고 제거용
}
