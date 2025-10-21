package recolnat.org.authorisation.article.api.web;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@Slf4j
class DateUtilTest {
    @Test
    void testParse() {
        var now = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")).toString();

        LocalDateTime ldt = LocalDateTime.parse(now, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        log.info("result of parse now :{}, result :{}", now, ldt);
    }
}
