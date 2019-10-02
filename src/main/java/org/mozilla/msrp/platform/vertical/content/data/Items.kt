package org.mozilla.msrp.platform.vertical.content.data

data class Item(
        var source: String? = null,
        var imageUrl: String? = null,
        var linkUrl: String? = null,
        var title: String? = null,
        var componentId: String? = null,
        var price: String = "",
        var discount: String = "",
        var rating: Double = 0.0,
        var reviews: Int = 0,
        var description: String? = null

)