package com.wowraid.jobspooncrawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties( { com.wowraid.jobspooncrawler.remember.config.RememberProperties.class } )
public class JobspoonCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobspoonCrawlerApplication.class, args);
    }

}
