package io.leonard;

import static ch.poole.openinghoursparser.RuleModifier.Modifier.*;

import ch.poole.openinghoursparser.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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

  public static Optional<LocalDateTime> isOpenNext(LocalDateTime time, List<Rule> rules) {
    return isOpenNext(time, rules, 0);
  };

  private static Optional<LocalDateTime> isOpenNext(
      LocalDateTime time, List<Rule> rules, int counter) {
    if (isOpenAt(time, rules)) return Optional.of(time);
    else {
      var open = getOpenRules(rules);
      var timeRangesOnThatDay =
          open.filter(rule -> timeMatchesDayRanges(time, rule.getDays()))
              .filter(r -> !Objects.isNull(r.getTimes()))
              .flatMap(r -> r.getTimes().stream().map(TimeRange::new));

      var closestOpeningThatDay =
          timeRangesOnThatDay
              .filter(range -> range.start.isAfter(time.toLocalTime()))
              .min(TimeRange.comparator)
              .map(timeRange -> time.toLocalDate().atTime(timeRange.start));

      if (counter >= MAX_SEARCH_DAYS) {
        return Optional.empty();
      }
      // if we cannot find time on the same day when the POI is open, we skip forward to the start
      // of the following day and try again
      else if (closestOpeningThatDay.isEmpty()) {
        var midnightNextDay = time.toLocalDate().plusDays(1).atStartOfDay();
        return isOpenNext(midnightNextDay, rules, ++counter);
      } else return closestOpeningThatDay;
    }
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
