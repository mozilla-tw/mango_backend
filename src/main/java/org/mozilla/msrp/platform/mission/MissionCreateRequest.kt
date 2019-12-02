package org.mozilla.msrp.platform.mission

class MissionCreateRequest {
    lateinit var missions: List<MissionCreateData>
}

data class MissionCreateData(
        val missionName: String,
        val titleId: String,
        val descriptionId: String,
        val missionType: String,
        val pings: List<String>,
        val startDate: String,
        val joinStartDate: String,
        val joinEndDate: String,
        val expiredDate: String,
        val redeemEndDate: String,
        val minVersion: Int,
        val missionParams: Map<String, Any>?,
        val rewardType: String,
        val joinQuota: Int,
        val imageUrl: String,
        val minVerDialogImage: String,
        val minVerDialogTitle: String,
        val minVerDialogMessage: String
)

class ValidationResult {
    private val error = mutableListOf<String>()

    fun addError(msg: String) {
        error.add(msg)
    }

    fun isValid(): Boolean {
        return error.isEmpty()
    }

    override fun toString(): String {
        return error.toString()
    }
}
