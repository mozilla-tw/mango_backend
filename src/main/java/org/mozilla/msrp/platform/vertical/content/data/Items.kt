package org.mozilla.msrp.platform.vertical.content.data

data class Item(
        val source: String,
        val imageUrl: String,
        val linkUrl: String,
        val title: String,
        val componentId: String,
        val price: String = "",
        var discount: String = "",
        var rating: Double = 0.0,
        var reviews: Int = 0
)