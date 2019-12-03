package org.mozilla.msrp.platform.admin

import org.mozilla.msrp.platform.mission.MissionRepository
import org.mozilla.msrp.platform.mission.qualifier.DailyMissionProgressDoc
import org.mozilla.msrp.platform.redward.RewardCouponDoc
import org.mozilla.msrp.platform.redward.RewardRepository
import org.mozilla.msrp.platform.user.UserRepository
import org.mozilla.msrp.platform.user.data.UserDoc
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject

@RestController
class ReportController @Inject constructor(
        private val userRepository: UserRepository,
        private val missionRepository: MissionRepository,
        private val rewardRepository: RewardRepository) {

    @GetMapping("/api/v1/report/user/activity")
    fun reportUserSuspended(): List<UserDoc> {
        return userRepository.reportUserActivity()
    }

    @GetMapping("/api/v1/report/reward")
    fun reportRewardCoupon(@RequestParam rewardId: String): List<RewardCouponDoc> {
        return rewardRepository.reportRewardCoupon(rewardId)
    }

    @GetMapping("/api/v1/report/mission/joined")
    fun reportMissionJoined(
            @RequestParam missionType: String,
            @RequestParam mid: String): List<DailyMissionProgressDoc> {
        return missionRepository.getProgress(missionType, mid)
    }

}