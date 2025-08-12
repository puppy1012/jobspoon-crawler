package com.wowraid.jobspooncrawler.remember.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@EnableConfigurationProperties(RememberProperties.class)
@TestPropertySource(properties = {
        "remember.base-url=https://career.rememberapp.co.kr/job/postings",
        "remember.wait-millis=4000",
        "remember.list-selector=li > div > a[rel=\"noopener noreferrer\"]"
})
class RememberPropertiesTest {
    @Autowired RememberProperties rememberProperties;

    @Test
    void test_remember_properties_isNotBlank() {
        assertThat(rememberProperties.getBaseUrl()).isEqualTo("https://career.rememberapp.co.kr/job/postings");
        assertThat(rememberProperties.getWaitMillis()).isEqualTo(4000);
        assertThat(rememberProperties.getListSelector()).isEqualTo("li > div > a[rel=\"noopener noreferrer\"]");
    }

}