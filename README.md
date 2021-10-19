# OSM opening hours evaluator

This Java package takes opening hours in OSM format and an instance of `java.time.LocalDateTime` and checks
if the POI is open or not at the specified time.

### Installation

![Maven Central](https://img.shields.io/maven-central/v/io.leonard/opening-hours-evaluator)

It's [available on Maven Central](https://mvnrepository.com/artifact/io.leonard/opening-hours-evaluator).

```
compile group: 'io.leonard', name: 'opening-hours-evaluator', version: '<latest version>'
```

### Usage

#### Is it open now?
```java
var openingHours = "Mo-Fr 09:00-18:00"
var parser = new OpeningHoursParser(new ByteArrayInputStream(openingHours.getBytes()));
var rules = parser.rules(true);
var time = LocalDateTime.parse("2020-08-07T12:09:17");
OpeningHoursEvaluator.isOpenAt(time, rules);
```

#### When does it close then?
```java
var openingHours = "Mo-Fr 09:00-18:00"
var parser = new OpeningHoursParser(new ByteArrayInputStream(openingHours.getBytes()));
var rules = parser.rules(true);
var time = LocalDateTime.parse("2020-08-07T12:09:17");
OpeningHoursEvaluator.isOpenAtUntil(time, rules);
```

#### When is it open next?

```java
var openingHours = "Mo-Fr 09:00-18:00"
var parser = new OpeningHoursParser(new ByteArrayInputStream(openingHours.getBytes()));
var rules = parser.rules(true);
var time = LocalDateTime.parse("2020-08-07T12:09:17");
OpeningHoursEvaluator.isOpenNext(time, rules);
```

#### When was it last open?

```java
var openingHours = "Mo-Fr 09:00-18:00"
var parser = new OpeningHoursParser(new ByteArrayInputStream(openingHours.getBytes()));
var rules = parser.rules(true);
var time = LocalDateTime.parse("2020-08-07T12:09:17");
OpeningHoursEvaluator.wasOpenLast(time, rules);
```

### Completeness

Since the [opening hours specification](https://wiki.openstreetmap.org/wiki/Key:opening_hours/specification) is complex, 
not every feature is supported. To get an idea of what exactly is working, please check the unit test input files 
[`open.csv`](https://github.com/leonardehrenfried/opening-hours-evaluator/blob/master/src/test/resources/open.csv) 
and [`closed.csv`](https://github.com/leonardehrenfried/opening-hours-evaluator/blob/master/src/test/resources/closed.csv).
