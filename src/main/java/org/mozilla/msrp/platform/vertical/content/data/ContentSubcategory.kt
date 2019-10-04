package org.mozilla.msrp.platform.vertical.content.data

data class ContentSubcategory(
        var componentType: String = "",
        var subcategoryName: String = "",
        var subcategoryId: Int = 0,
        var items: List<ContentItem> = listOf()
)