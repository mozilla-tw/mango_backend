package org.mozilla.msrp.platform.push.model

import com.google.gson.annotations.SerializedName


data class StmoResponse(
        @SerializedName("query_result") val query_result: Query_result
)

data class Query_result(

        @SerializedName("id") val id: Int,
        @SerializedName("query_hash") val query_hash: String,
        @SerializedName("query") val query: String,
        @SerializedName("data") val data: Data,
        @SerializedName("data_source_id") val data_source_id: Int,
        @SerializedName("runtime") val runtime: Double,
        @SerializedName("retrieved_at") val retrieved_at: String
)

data class Metadata(

        @SerializedName("data_scanned") val data_scanned: Int
)

data class Data(

        @SerializedName("rows") val rows: List<Rows>,
        @SerializedName("columns") val columns: List<Columns>,
        @SerializedName("metadata") val metadata: Metadata
)

data class Rows(

        @SerializedName("client_id") val client_id: String
)

data class Columns(

        @SerializedName("type") val type: String,
        @SerializedName("friendly_name") val friendly_name: String,
        @SerializedName("name") val name: String
)

