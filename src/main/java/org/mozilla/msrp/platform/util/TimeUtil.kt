package org.mozilla.msrp.platform.util

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun getDayDifference(start: ZonedDateTime, end: ZonedDateTime): Long {
    require(start.zone == end.zone)

    val zone = start.zone

    val startDay = start.toLocalDate().atStartOfDay(zone)
    val endDay = end.toLocalDate().atStartOfDay(zone)

    return ChronoUnit.DAYS.between(startDay, endDay)
}

fun getMinuteOfDayDifference(start: ZonedDateTime, end: ZonedDateTime): Long {
    val startMinute = ChronoUnit.MINUTES.between(
            start.toStartOfDay(),
            start
    )

    val endMinute = ChronoUnit.MINUTES.between(
            end.toStartOfDay(),
            end
    )

    return endMinute - startMinute
}

fun ZonedDateTime.toStartOfDay(): ZonedDateTime {
    return this.toLocalDate().atStartOfDay(zone)
}
