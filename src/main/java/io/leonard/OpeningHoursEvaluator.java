package io.leonard;

import ch.poole.openinghoursparser.Rule;
import ch.poole.openinghoursparser.TimeSpan;
import ch.poole.openinghoursparser.WeekDay;
import ch.poole.openinghoursparser.WeekDayRange;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

public class OpeningHoursEvaluator {

    public static boolean isOpenAt(LocalDateTime time, List<Rule> rules) {
        return rules.stream().anyMatch(rule -> rule.isTwentyfourseven() || isOpenAt(time, rule));
    }

    private static boolean isOpenAt(LocalDateTime time, Rule rule) {
        return nullToEmptyList(rule.getDays()).stream()
                        .anyMatch(dayRange -> isOpenAtDay(time, dayRange))
                && nullToEmptyList(rule.getTimes()).stream()
                        .anyMatch(timeSpan -> isOpenAtTime(time, timeSpan));
    }

    private static boolean isOpenAtDay(LocalDateTime time, WeekDayRange range) {
        int ordinal = time.getDayOfWeek().ordinal();
        return range.getStartDay().ordinal() <= ordinal && isBeforeEnd(ordinal, range.getEndDay());
    }

    private static boolean isBeforeEnd(int ordinal, WeekDay weekDay) {
        // if the end day is null it means that it's just a single day like in "Th 10:00-18:00"
        if(weekDay == null) return true;
        else return weekDay.ordinal() >= ordinal;
    }

    private static boolean isOpenAtTime(LocalDateTime time, TimeSpan timeSpan) {
        var minutesAfterMidnight = minutesAfterMidnight(time.toLocalTime());
        return timeSpan.getStart() <= minutesAfterMidnight
                && timeSpan.getEnd() >= minutesAfterMidnight;
    }

    private static int minutesAfterMidnight(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private static <T> List<T> nullToEmptyList(List<T> list) {
        if (list == null) return Collections.emptyList();
        else return list;
    }
}
