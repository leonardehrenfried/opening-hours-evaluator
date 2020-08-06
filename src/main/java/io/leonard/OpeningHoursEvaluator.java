package io.leonard;

import ch.poole.openinghoursparser.Rule;

import java.time.LocalDateTime;
import java.util.List;

public class OpeningHoursEvaluator {

    public static boolean isOpenAt(LocalDateTime time, List<Rule> rules) {
        return true;
    }
}
