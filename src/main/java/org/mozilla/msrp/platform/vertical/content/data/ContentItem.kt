package org.mozilla.msrp.platform.vertical.content.data

import com.opencsv.bean.CsvBindByName

// TODO: set the required fields for each categories
//  https://docs.google.com/spreadsheets/d/1s--x8TiIsEEISHl3YJA8ulAljxJt4FQUHZ0r5tCxHy0/edit#gid=1268475188
class ContentItem(
        @field:CsvBindByName(required = false) var created_at: Long? = 0L,
        @field:CsvBindByName(required = false) var country: String? = "",
        @field:CsvBindByName(required = false) var source_name: String? = "",
        @field:CsvBindByName(required = false) var source_type: String? = "",
        @field:CsvBindByName(required = false) var partner: Boolean? = false,
        @field:CsvBindByName(required = false) var vertical_name: String? = "",
        @field:CsvBindByName(required = false) var vertical_id: Int? = 0,
        @field:CsvBindByName(required = false) var category_name: String? = "",
        @field:CsvBindByName(required = false) var category_id: Int? = 0,
        @field:CsvBindByName(required = false) var subcategory_name: String? = "",
        @field:CsvBindByName(required = false) var subcategory_id: Int? = 0,
        @field:CsvBindByName(required = false) var component_type_name: String? = "",
        @field:CsvBindByName(required = false) var component_type_id: Int? = 0,
        @field:CsvBindByName(required = false) var image_type: String? = "",
        @field:CsvBindByName(required = false) var image: String? = "",
        @field:CsvBindByName(required = false) var destination: String = "",
        @field:CsvBindByName(required = false) var title: String? = "",
        @field:CsvBindByName(required = false) var description: String? = "",
        @field:CsvBindByName(required = false) var score: Double? = 0.0,
        @field:CsvBindByName(required = false) var score_reviews: Int? = 0,
        @field:CsvBindByName(required = false) var price: String? = "",
        @field:CsvBindByName(required = false) var discount: String? = "",
        @field:CsvBindByName(required = false) var fresh: Boolean? = false,
        @field:CsvBindByName(required = false) var start_date: Long? = 0L,
        @field:CsvBindByName(required = false) var end_date: Long? = 0L,
        @field:CsvBindByName(required = false) var additional: String = "",
        var component_id: String = ""
)