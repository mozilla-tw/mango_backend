package org.mozilla.msrp.platform.push.service

import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

@Named
class StmoService @Inject constructor(private val stmoClient: StmoClient) {

    fun loadClientIds(stmoUrl: String): StmoServiceResponse? {
        try {
            val list = stmoClient.fromUrl(stmoUrl).execute().body()?.query_result?.data?.rows?.map { it.client_id }
                    ?: return StmoServiceResponse.Error("No User Found in STMO Link")
            return StmoServiceResponse.Success(list)
        } catch (e: IOException) {
            return StmoServiceResponse.Error("[loadClientIds]${e.message}")
        }
    }
}

sealed class StmoServiceResponse {
    class Success(val list: List<String>) : StmoServiceResponse()
    class Error(val error: String) : StmoServiceResponse()
}