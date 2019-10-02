package org.mozilla.msrp.platform.vertical.content.data

data class Subcategory(
        var componentType: String = "",
        var subcategoryName: String = "",
        var subcategoryId: Int = 0,
        var items: List<Item> = listOf()
)