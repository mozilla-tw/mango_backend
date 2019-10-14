package org.mozilla.msrp.platform.mission.qualifier

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.concurrent.TimeUnit
import javax.inject.Named

@Named
@ConfigurationProperties("mission.daily")
data class DailyMissionProperties(
        var checkInIntervalSeconds: Long = TimeUnit.DAYS.toSeconds(1)
)
