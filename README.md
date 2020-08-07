# OSM opening hours evaluator

This Java package takes opening hours in OSM format and an instance of `java.time.LocalDateTime` and checks
if the POI is open or not at the specified time.

### Installation

It's available on Maven Central.

```
compile group: 'io.leonard', name: 'opening-hours-evaluator', version: '0.0.3'
```
It depends on Simon Poole's `OpeningHoursParser` but since that is not on Maven Central, the dependency is bundled 
inside the Jar.

### Usage

```java
var openingHours = "Mo-Fr 09:00-18:00"
var parser = new OpeningHoursParser(new ByteArrayInputStream(openingHours.getBytes()));
var rules = parser.rules(true);
var time = LocalDateTime.parse("2020-08-07T12:09:17")
OpeningHoursEvaluator.isOpenAt(time, rules)
```



