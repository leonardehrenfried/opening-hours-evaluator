package io.leonard;

import ch.poole.openinghoursparser.OpeningHoursParseException;
import ch.poole.openinghoursparser.OpeningHoursParser;
import ch.poole.openinghoursparser.Rule;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpeningHoursEvaluatorTest {

    @Test
    void simpleOpeningHours() throws Exception {
        var openingHours = "24/7";
        var rules = parseOpeningHours(openingHours);
        var time = LocalDateTime.parse("2020-08-06T14:28:04");
        assertTrue(OpeningHoursEvaluator.isOpenAt(time, rules));
    }

    private List<Rule> parseOpeningHours(String openingHours) throws OpeningHoursParseException {
        var parser = new OpeningHoursParser(new ByteArrayInputStream(openingHours.getBytes()));
        return parser.rules(true);
    }
}
