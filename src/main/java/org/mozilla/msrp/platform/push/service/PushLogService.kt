package org.mozilla.msrp.platform.push.service

import org.mozilla.msrp.platform.push.repository.PushLogRepository
import javax.inject.Inject
import javax.inject.Named

@Named
class PushLogService @Inject constructor(val pubsubRepository: PushLogRepository) {

    fun addWork(data: String): Int? {
        return pubsubRepository.addWork(data)
    }
}