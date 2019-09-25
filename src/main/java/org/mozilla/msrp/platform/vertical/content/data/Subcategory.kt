package org.mozilla.msrp.platform.vertical.content.data

data class Subcategory(
        val componentType: String,
        val subcategoryName: String,
        val subcategoryId: Int,
        var items: List<Item>
)