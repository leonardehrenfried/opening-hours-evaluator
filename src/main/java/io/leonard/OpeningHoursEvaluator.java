package io.leonard;

import static ch.poole.openinghoursparser.RuleModifier.Modifier.CLOSED;
import static ch.poole.openinghoursparser.RuleModifier.Modifier.OFF;
import static ch.poole.openinghoursparser.RuleModifier.Modifier.OPEN;
import static ch.poole.openinghoursparser.RuleModifier.Modifier.UNKNOWN;

import ch.poole.openinghoursparser.Rule;
import ch.poole.openinghoursparser.RuleModifier;
import ch.poole.openinghoursparser.TimeSpan;
import ch.poole.openinghoursparser.WeekDay;
import ch.poole.openinghoursparser.WeekDayRange;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class OpeningHoursEvaluator {

  private static final Set<RuleModifier.Modifier> CLOSED_MODIFIERS = Set.of(CLOSED, OFF);
  private static final Set<RuleModifier.Modifier> OPEN_MODIFIERS = Set.of(OPEN, UNKNOWN);

  // when calculating the next time the hours are open, how many days should you go into the future
  // this protects against stack overflows when the place is never going to open again
  private static final int MAX_SEARCH_DAYS = 365 * 10;

  public static boolean isOpenAt(LocalDateTime time, List<Rule> rules) {
    var closed = getClosedRules(rules);
    var open = getOpenRules(rules);
    return closed.noneMatch(rule -> timeMatchesRule(time, rule))
        && open.anyMatch(rule -> rule.isTwentyfourseven() || timeMatchesRule(time, rule));
  }

  public static Optional<LocalDateTime> wasLastOpen(LocalDateTime time, List<Rule> rules) {
    return isOpenIterative(time, rules, false, MAX_SEARCH_DAYS);
  };

  public static Optional<LocalDateTime> wasLastOpen(
      LocalDateTime time, List<Rule> rules, int searchDays) {
    return isOpenIterative(time, rules, false, searchDays);
  };

  public static Optional<LocalDateTime> isOpenNext(LocalDateTime time, List<Rule> rules) {
    return isOpenIterative(time, rules, true, MAX_SEARCH_DAYS);
  };

  public static Optional<LocalDateTime> isOpenNext(
      LocalDateTime time, List<Rule> rules, int searchDays) {
    return isOpenIterative(time, rules, true, searchDays);
  };

  private static Optional<LocalDateTime> isOpenIterative(
      final LocalDateTime initialTime,
      final List<Rule> rules,
      boolean forward,
      final int searchDays) {

    var nextTime = initialTime;
    for (var iterations = 0; iterations <= searchDays; ++iterations) {
      var open = getOpenRules(rules);
      var closed = getClosedRules(rules);

      var time = nextTime;
      if (isOpenAt(time, rules)) return Optional.of(time);
      else {

        var openRangesOnThatDay = getTimeRangesOnThatDay(time, open);
        var closedRangesThatDay = getTimeRangesOnThatDay(time, closed);

        var endOfExclusion =
            forward
                ? closedRangesThatDay
                    .filter(r -> r.surrounds(time.toLocalTime()))
                    .findFirst()
                    .map(r -> time.toLocalDate().atTime(r.end))
                : closedRangesThatDay
                    .filter(r -> r.surrounds(time.toLocalTime()))
                    .findFirst()
                    .map(r -> time.toLocalDate().atTime(r.start));

        var startOfNextOpening =
            forward
                ? openRangesOnThatDay
                    .filter(range -> range.start.isAfter(time.toLocalTime()))
                    .min(TimeRange.startComparator)
                    .map(timeRange -> time.toLocalDate().atTime(timeRange.start))
                : openRangesOnThatDay
                    .filter(range -> range.end.isBefore(time.toLocalTime()))
                    .max(TimeRange.endComparator)
                    .map(timeRange -> time.toLocalDate().atTime(timeRange.end));

        var opensNextThatDay = endOfExclusion.or(() -> startOfNextOpening);
        if (opensNextThatDay.isPresent()) {
          return opensNextThatDay;
        }

        // if we cannot find time on the same day when the POI is open, we skip forward to the start
        // of the following day and try again
        nextTime =
            forward
                ? time.toLocalDate().plusDays(1).atStartOfDay()
                : time.toLocalDate().minusDays(1).atTime(LocalTime.MAX);
      }
    }

    return Optional.empty();
  }

  private static Stream<TimeRange> getTimeRangesOnThatDay(LocalDateTime time, Stream<Rule> open) {
    return open.filter(rule -> timeMatchesDayRanges(time, rule.getDays()))
        .filter(r -> !Objects.isNull(r.getTimes()))
        .flatMap(r -> r.getTimes().stream().map(TimeRange::new));
  }

  private static Stream<Rule> getOpenRules(List<Rule> rules) {
    return rules.stream()
        .filter(
            r -> {
              var modifier = r.getModifier();
              return modifier == null || OPEN_MODIFIERS.contains(modifier.getModifier());
            });
  }

  private static Stream<Rule> getClosedRules(List<Rule> rules) {
    return rules.stream()
        .filter(
            r ->
                r.getModifier() != null
                    && CLOSED_MODIFIERS.contains(r.getModifier().getModifier()));
  }

  private static boolean timeMatchesRule(LocalDateTime time, Rule rule) {
    return timeMatchesDayRanges(time, rule.getDays())
        && nullToEntireDay(rule.getTimes()).stream()
            .anyMatch(timeSpan -> timeMatchesHours(time, timeSpan));
  }

  private static boolean timeMatchesDayRanges(LocalDateTime time, List<WeekDayRange> ranges) {
    return nullToEmptyList(ranges).stream().anyMatch(dayRange -> timeMatchesDay(time, dayRange));
  }

  private static boolean timeMatchesDay(LocalDateTime time, WeekDayRange range) {
    // if the end day is null it means that it's just a single day like in "Th
    // 10:00-18:00"
    if (range.getEndDay() == null) {
      return time.getDayOfWeek().equals(toDayOfWeek(range.getStartDay()));
    }
    int ordinal = time.getDayOfWeek().ordinal();
    return range.getStartDay().ordinal() <= ordinal && range.getEndDay().ordinal() >= ordinal;
  }

  private static boolean timeMatchesHours(LocalDateTime time, TimeSpan timeSpan) {
    var minutesAfterMidnight = minutesAfterMidnight(time.toLocalTime());
    return timeSpan.getStart() <= minutesAfterMidnight && timeSpan.getEnd() >= minutesAfterMidnight;
  }

  private static int minutesAfterMidnight(LocalTime time) {
    return time.getHour() * 60 + time.getMinute();
  }

  private static <T> List<T> nullToEmptyList(List<T> list) {
    if (list == null) return Collections.emptyList();
    else return list;
  }

  private static List<TimeSpan> nullToEntireDay(List<TimeSpan> span) {
    if (span == null) {
      var allDay = new TimeSpan();
      allDay.setStart(TimeSpan.MIN_TIME);
      allDay.setEnd(TimeSpan.MAX_TIME);
      return List.of(allDay);
    } else return span;
  }

  private static DayOfWeek toDayOfWeek(WeekDay day) {
    if (day == WeekDay.MO) return DayOfWeek.MONDAY;
    else if (day == WeekDay.TU) return DayOfWeek.TUESDAY;
    else if (day == WeekDay.WE) return DayOfWeek.WEDNESDAY;
    else if (day == WeekDay.TH) return DayOfWeek.THURSDAY;
    else if (day == WeekDay.FR) return DayOfWeek.FRIDAY;
    else if (day == WeekDay.SA) return DayOfWeek.SATURDAY;
    else if (day == WeekDay.SU) return DayOfWeek.SUNDAY;
    else return null;
  }
}
