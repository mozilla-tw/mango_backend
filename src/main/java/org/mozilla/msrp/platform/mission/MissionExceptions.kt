package org.mozilla.msrp.platform.mission

import java.lang.RuntimeException

open class MissionException(message: String, cause: Throwable) : RuntimeException(message, cause)

class MissionNotFoundException(missionType: String, mid: String, cause: Throwable)
    : MissionException("Fail to find mission /$missionType/$mid", cause)
