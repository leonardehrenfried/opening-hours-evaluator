package io.leonard;

import ch.poole.openinghoursparser.OpeningHoursParseException;
import ch.poole.openinghoursparser.OpeningHoursParser;
import ch.poole.openinghoursparser.Rule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpeningHoursEvaluatorTest {

  @ParameterizedTest
  @CsvFileSource(resources = "/open.csv")
  void shouldEvaluateAsOpen(String time, String openingHours) throws OpeningHoursParseException {
    var rules = parseOpeningHours(openingHours);
    var parsed = LocalDateTime.parse(time);
    assertTrue(OpeningHoursEvaluator.isOpenAt(parsed, rules));
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/closed.csv")
  void shouldEvaluateAsClosed(String time, String openingHours) throws OpeningHoursParseException {
    var rules = parseOpeningHours(openingHours);
    var parsed = LocalDateTime.parse(time);
    assertFalse(OpeningHoursEvaluator.isOpenAt(parsed, rules));
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/open-next.csv", numLinesToSkip = 1)
  void shouldCalculateWhenItsOpenNext(String time, String openingHours, String openNext)
      throws OpeningHoursParseException {
    var rules = parseOpeningHours(openingHours);
    var parsed = LocalDateTime.parse(time);
    var openNextTime = LocalDateTime.parse(openNext);
    assertEquals(openNextTime, OpeningHoursEvaluator.isOpenNext(parsed, rules).get());
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/was-last-open.csv", numLinesToSkip = 1)
  void shouldCalculateWhenItWasLastOpen(String time, String openingHours, String openBefore)
          throws OpeningHoursParseException {
    var rules = parseOpeningHours(openingHours);
    var parsed = LocalDateTime.parse(time);
    var lastOpenTime = LocalDateTime.parse(openBefore);
    assertEquals(lastOpenTime, OpeningHoursEvaluator.wasLastOpen(parsed, rules).get());
  }

  private List<Rule> parseOpeningHours(String openingHours) throws OpeningHoursParseException {
    var parser = new OpeningHoursParser(new ByteArrayInputStream(openingHours.getBytes()));
    return parser.rules(true);
  }
}
