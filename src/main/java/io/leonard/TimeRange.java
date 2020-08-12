package io.leonard;

import ch.poole.openinghoursparser.TimeSpan;
import java.time.LocalTime;

public class TimeRange {

  public final LocalTime start;
  public final LocalTime end;

  public TimeRange(TimeSpan span) {
    this.start = LocalTime.ofSecondOfDay(span.getStart() * 60);
    this.end = LocalTime.ofSecondOfDay(span.getEnd() * 60);
  }
}
