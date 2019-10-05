package org.mozilla.msrp.platform.vertical.content.data

data class Category(
        var subcategories: List<ContentSubcategory> = listOf(),
        var version: Int = 1
)