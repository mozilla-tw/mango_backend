package org.mozilla.msrp.platform.vertical.content.data

data class ContentResponse(
        var version: Int = 1,
        var subcategories: List<Subcategory> = listOf()
)