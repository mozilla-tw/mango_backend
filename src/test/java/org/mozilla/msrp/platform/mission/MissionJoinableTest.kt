package org.mozilla.msrp.platform.mission

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mozilla.msrp.platform.util.logger
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MissionJoinableTest {

    private val zoneUtc = ZoneId.of("GMT+0")
    private val zoneJakarta = ZoneId.of("GMT+7")

    private val beforeStartJakarta = LocalDateTime.parse("2019-10-22T23:59:59")

    private val afterStartJakarta = LocalDateTime.parse("2019-10-23T00:00:00")
    private val afterStartUtc = LocalDateTime.ofInstant(afterStartJakarta.atZone(zoneJakarta).toInstant(), zoneUtc)

    private val afterEndJakarta = LocalDateTime.parse("2019-10-26T00:00:00")
    private val afterEndUtc = LocalDateTime.ofInstant(afterEndJakarta.atZone(zoneJakarta).toInstant(), zoneUtc)

    private val afterExpiredJakarta = LocalDateTime.parse("2019-11-02T00:00:00")
    private val afterExpiredUtc = LocalDateTime.ofInstant(afterExpiredJakarta.atZone(zoneJakarta).toInstant(), zoneUtc)

    private val missionRepo = mock(MissionRepository::class.java)
    private val service = MissionService(missionRepo)

    @Test
    fun `beforeStart + (any status) + (any quota) = notVisible`() {
        testVisibility()
                .periodNotStart()
                .forAllStatus()
                .flatMap { it.forAllQuota() }
                .apply { logger().info("combinations=$size") }
                .forEach { it.test(false) }
    }

    @Test
    fun `afterStart + notJoin + hasQuota = visible`() {
        testVisibility()
                .periodStart()
                .statusNotJoin()
                .quotaAvailable()
                .test(true)
    }

    @Test
    fun `afterStart + notJoin + noQuota = invisible`() {
        testVisibility()
                .periodStart()
                .statusNotJoin()
                .quotaUnavailable()
                .test(false)
    }

    @Test
    fun `afterStart + joined + (any quota) = visible`() {
        testVisibility()
                .periodStart()
                .statusJoined()
                .forAllQuota()
                .forEach { it.test(true) }
    }

    @Test
    fun `afterEnd + notJoin + (any quota) = invisible`() {
        testVisibility()
                .periodEnd()
                .statusNotJoin()
                .forAllQuota()
                .forEach { it.test(false) }
    }

    @Test
    fun `afterEnd + joined + (any quota) = visible`() {
        testVisibility()
                .periodEnd()
                .statusJoined()
                .forAllQuota()
                .forEach { it.test(true) }
    }

    @Test
    fun `expired + notComplete + (any quota) = invisible`() {
        testVisibility()
                .periodExpired()
                .statusNotJoin()
                .forAllQuota()
                .forEach { it.test(false) }

        testVisibility()
                .periodExpired()
                .statusJoined()
                .forAllQuota()
                .forEach { it.test(false) }
    }

    @Test
    fun `expired + completed + (any quota) = visible`() {
        testVisibility()
                .periodExpired()
                .statusCompleted()
                .forAllQuota()
                .forEach { it.test(true) }
    }

    private fun testVisibility(): JoinPeriodConfig {
        return JoinPeriodConfig()
    }

    private fun LocalDateTime.toInstant(zone: ZoneId): Instant {
        return atZone(zone).toInstant()
    }

    private fun createMissionDoc(): MissionDoc {
        return MissionDoc(
                joinStartDate = afterStartUtc.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                joinEndDate = afterEndUtc.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                expiredDate = afterExpiredUtc.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                joinQuota = 10
        )
    }

    inner class JoinPeriodConfig {
        fun periodNotStart(): JoinStatusConfig {
            return JoinStatusConfig(beforeStartJakarta)
        }

        fun periodStart(): JoinStatusConfig {
            return JoinStatusConfig(afterStartJakarta)
        }

        fun periodEnd(): JoinStatusConfig {
            return JoinStatusConfig(afterEndJakarta)
        }

        fun periodExpired(): JoinStatusConfig {
            return JoinStatusConfig(afterExpiredJakarta)
        }

        fun forAllPeriod(): List<JoinStatusConfig> {
            return listOf(periodNotStart(), periodStart(), periodEnd(), periodExpired())
        }
    }

    inner class JoinStatusConfig(private val dateTime: LocalDateTime) {
        fun statusNotJoin(): JoinQuotaConfig {
            return JoinQuotaConfig(dateTime, JoinStatus.New)
        }

        fun statusJoined(): JoinQuotaConfig {
            return JoinQuotaConfig(dateTime, JoinStatus.Joined)
        }

        fun statusCompleted(): JoinQuotaConfig {
            return JoinQuotaConfig(dateTime, JoinStatus.Complete)
        }

        fun statusRedeemed(): JoinQuotaConfig {
            return JoinQuotaConfig(dateTime, JoinStatus.Redeemed)
        }

        fun forAllStatus(): List<JoinQuotaConfig> {
            return listOf(statusNotJoin(), statusJoined(), statusCompleted(), statusRedeemed())
        }
    }

    inner class JoinQuotaConfig(private val dateTime: LocalDateTime, private val status: JoinStatus) {
        fun quotaAvailable(): VisibilityTestConfig {
            return VisibilityTestConfig(dateTime, status, 5)
        }

        fun quotaUnavailable(): VisibilityTestConfig {
            return VisibilityTestConfig(dateTime, status, 10)
        }

        fun forAllQuota(): List<VisibilityTestConfig> {
            return listOf(quotaAvailable(), quotaUnavailable())
        }
    }

    inner class VisibilityTestConfig(
            private val dateTime: LocalDateTime,
            private val status: JoinStatus,
            private val count: Int
    ) {

        fun test(expected: Boolean) {
            val fixInstant = dateTime.toInstant(zoneJakarta)
            val clock = Clock.fixed(fixInstant, zoneJakarta)

            assertEquals(expected, service.isMissionAvailableForShowing(
                    "uid",
                    createMissionDoc(),
                    status,
                    count,
                    clock,
                    zoneJakarta
            ))
        }
    }
}
