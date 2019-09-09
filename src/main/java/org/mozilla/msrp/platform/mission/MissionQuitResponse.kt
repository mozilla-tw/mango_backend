package org.mozilla.msrp.platform.mission

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

sealed class MissionQuitResponse {
    class Success(val mid: String, val status: JoinStatus) : MissionQuitResponse()
    class Error(val error: String, @JsonIgnore val code: HttpStatus) : MissionQuitResponse()
    object Empty : MissionQuitResponse()

    fun toEntityResponse(): ResponseEntity<MissionQuitResponse> {
        return when {
            this is Success -> ResponseEntity.ok(this)
            this is Error -> ResponseEntity.status(code).body(this)
            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Empty)
        }
    }
}
