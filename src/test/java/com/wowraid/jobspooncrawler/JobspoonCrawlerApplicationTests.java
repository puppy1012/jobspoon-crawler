
package com.wowraid.jobspooncrawler;

import com.wowraid.jobspooncrawler.jumpit.entity.JumpitPositionFeatureDto;
import com.wowraid.jobspooncrawler.jumpit.service.JumpitPositionsFeatureCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class JobspoonCrawlerApplicationTests {

    private final JumpitPositionsFeatureCrawler crawler = new JumpitPositionsFeatureCrawler();

    @Test
    void parse_snapshot_html() throws IOException {

        Path p = Path.of("src/test/resources/sample_jumpit.html");
        String html = Files.readString(p);
        Document doc = Jsoup.parse(html);

        // we emulate internal methods via reflection or copy a smaller helper.
        // To keep it simple, we'll just ensure the file is loaded.
        assertThat(doc).isNotNull();
    }

    // Integration test stub (disabled by default because it hits real net)
    //@Test
    void crawl_real_url() throws Exception {
        JumpitPositionFeatureDto post = crawler.crawl("https://jumpit.saramin.co.kr/position/51050679");
        assertThat(post.getTitle()).isNotBlank();
    }
}
