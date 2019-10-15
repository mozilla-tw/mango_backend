package org.mozilla.msrp.platform.mission.qualifier

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mozilla.msrp.platform.firestore.stringToLocalDateTime
import org.mozilla.msrp.platform.util.getDayDifference
import org.mozilla.msrp.platform.util.getMinuteOfDayDifference
import java.time.ZoneId
import java.time.ZonedDateTime

internal class DailyMissionQualifierTimeTest {

    private val day1T1 = "2019-08-10T08:00:00"
    private val day1T2 = "2019-08-10T10:00:00"
    private val day1End = "2019-08-10T23:59:59"

    private val day2Start = "2019-08-11T00:00:00"
    private val day2T1 = "2019-08-11T08:00:00"
    private val day2T2 = "2019-08-11T10:00:00"

    private val day3Start = "2019-08-12T00:00:00"


    private val min1T1 = "2019-08-10T00:00:20"
    private val min1T2 = "2019-08-10T00:00:30"
    private val min1End = "2019-08-10T00:00:59"

    private val min2Start = "2019-08-10T00:01:00"
    private val min2T1 = "2019-08-10T00:01:20"
    private val min2T2 = "2019-08-10T00:01:30"

    private val min3Start = "2019-08-10T00:02:00"

    @Test
    fun testGetDayDifference() {
        val zone = ZoneId.of("Asia/Taipei")

        // Any two moments within 1 day
        var t1 = ZonedDateTime.of(stringToLocalDateTime(day1T1), zone)
        var t2 = ZonedDateTime.of(stringToLocalDateTime(day1T2), zone)
        assertEquals(0, getDayDifference(t1, t2))

        // 24 hours within two days
        t1 = ZonedDateTime.of(stringToLocalDateTime(day1T1), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(day2T1), zone)
        assertEquals(1, getDayDifference(t1, t2))

        // less than 24 hour within two days
        t1 = ZonedDateTime.of(stringToLocalDateTime(day1T2), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(day2T1), zone)
        assertEquals(1, getDayDifference(t1, t2))

        // over 24 hour within two days
        t1 = ZonedDateTime.of(stringToLocalDateTime(day1T1), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(day2T2), zone)
        assertEquals(1, getDayDifference(t1, t2))

        // Day change moment
        t1 = ZonedDateTime.of(stringToLocalDateTime(day1End), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(day2Start), zone)
        assertEquals(1, getDayDifference(t1, t2))

        // End of the first day & start of the third day
        t1 = ZonedDateTime.of(stringToLocalDateTime(day1End), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(day3Start), zone)
        assertEquals(true, getDayDifference(t1, t2) >= 2)
    }

    @Test
    fun testMinuteOfDayDifference() {
        val zone = ZoneId.of("Asia/Taipei")

        // Any two moments within 1 minute
        var t1 = ZonedDateTime.of(stringToLocalDateTime(min1T1), zone)
        var t2 = ZonedDateTime.of(stringToLocalDateTime(min1T2), zone)
        assertEquals(0, getMinuteOfDayDifference(t1, t2))

        // 60 seconds within two minutes
        t1 = ZonedDateTime.of(stringToLocalDateTime(min1T1), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(min2T1), zone)
        assertEquals(1, getMinuteOfDayDifference(t1, t2))

        // less than 60 seconds within two minutes
        t1 = ZonedDateTime.of(stringToLocalDateTime(min1T2), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(min2T1), zone)
        assertEquals(1, getMinuteOfDayDifference(t1, t2))

        // over 60 seconds within two minutes
        t1 = ZonedDateTime.of(stringToLocalDateTime(min1T1), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(min2T2), zone)
        assertEquals(1, getMinuteOfDayDifference(t1, t2))

        // minute change moment
        t1 = ZonedDateTime.of(stringToLocalDateTime(min1End), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(min2Start), zone)
        assertEquals(1, getMinuteOfDayDifference(t1, t2))

        // End of the first minute & start of the third minute
        t1 = ZonedDateTime.of(stringToLocalDateTime(min1End), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(min3Start), zone)
        assertEquals(true, getMinuteOfDayDifference(t1, t2) >= 2)
    }

    @Test
    fun testProductionActionResolver() {
        val zone = ZoneId.of("Asia/Taipei")
        val resolver = DailyMissionQualifier.ProductionResolver()

        // Any two moments within 1 day
        var t1 = ZonedDateTime.of(stringToLocalDateTime(day1T1), zone)
        var t2 = ZonedDateTime.of(stringToLocalDateTime(day1T2), zone)
        assertEquals(DailyMissionQualifier.Action.NoAction, resolver.resolve(t1, t2))

        // 24 hours within two days
        t1 = ZonedDateTime.of(stringToLocalDateTime(day1T1), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(day2T1), zone)
        assertEquals(DailyMissionQualifier.Action.Advance, resolver.resolve(t1, t2))

        // less than 24 hour within two days
        t1 = ZonedDateTime.of(stringToLocalDateTime(day1T2), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(day2T1), zone)
        assertEquals(DailyMissionQualifier.Action.Advance, resolver.resolve(t1, t2))

        // over 24 hour within two days
        t1 = ZonedDateTime.of(stringToLocalDateTime(day1T1), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(day2T2), zone)
        assertEquals(DailyMissionQualifier.Action.Advance, resolver.resolve(t1, t2))

        // Day change moment
        t1 = ZonedDateTime.of(stringToLocalDateTime(day1End), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(day2Start), zone)
        assertEquals(DailyMissionQualifier.Action.Advance, resolver.resolve(t1, t2))

        // End of the first day & start of the third day
        t1 = ZonedDateTime.of(stringToLocalDateTime(day1End), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(day3Start), zone)
        assertEquals(DailyMissionQualifier.Action.Restart, resolver.resolve(t1, t2))
    }

    @Test
    fun testNightlyActionResolver() {
        val zone = ZoneId.of("Asia/Taipei")
        val resolver = DailyMissionQualifier.NightlyResolver()

        // Any two moments within 1 minute
        var t1 = ZonedDateTime.of(stringToLocalDateTime(min1T1), zone)
        var t2 = ZonedDateTime.of(stringToLocalDateTime(min1T2), zone)
        assertEquals(DailyMissionQualifier.Action.NoAction, resolver.resolve(t1, t2))

        // 60 seconds within two minutes
        t1 = ZonedDateTime.of(stringToLocalDateTime(min1T1), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(min2T1), zone)
        assertEquals(DailyMissionQualifier.Action.Advance, resolver.resolve(t1, t2))

        // less than 60 seconds within two minutes
        t1 = ZonedDateTime.of(stringToLocalDateTime(min1T2), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(min2T1), zone)
        assertEquals(DailyMissionQualifier.Action.Advance, resolver.resolve(t1, t2))

        // over 60 seconds within two minutes
        t1 = ZonedDateTime.of(stringToLocalDateTime(min1T1), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(min2T2), zone)
        assertEquals(DailyMissionQualifier.Action.Advance, resolver.resolve(t1, t2))

        // minute change moment
        t1 = ZonedDateTime.of(stringToLocalDateTime(min1End), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(min2Start), zone)
        assertEquals(DailyMissionQualifier.Action.Advance, resolver.resolve(t1, t2))

        // End of the first minute & start of the third minute
        t1 = ZonedDateTime.of(stringToLocalDateTime(min1End), zone)
        t2 = ZonedDateTime.of(stringToLocalDateTime(min3Start), zone)
        assertEquals(DailyMissionQualifier.Action.Restart, resolver.resolve(t1, t2))
    }
}
