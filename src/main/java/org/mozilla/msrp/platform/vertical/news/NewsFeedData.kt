package org.mozilla.msrp.platform.vertical.news

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Path
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text
import java.text.ParseException
import java.text.SimpleDateFormat


open class Rss<T> {
    open var feedItems: List<T>? = null
}

@Root(strict = false)
class DetikRss : Rss<DetikFeedItem>() {
    @field:Attribute
    var version: String? = null

    @field:Path("channel")
    @field:ElementList(name = "item", entry = "item", type = DetikFeedItem::class, required = true, inline = true)
    override var feedItems: List<DetikFeedItem>? = null
}

@Root(strict = false)
class Liputan6Rss : Rss<LiputanFeedItem>() {
    @field:Attribute
    var version: String? = null

    @field:Path("channel")
    @field:ElementList(name = "item", entry = "item", type = LiputanFeedItem::class, required = true, inline = true)
    override var feedItems: List<LiputanFeedItem>? = null
}

@Root(strict = false)
class GoogleRss : Rss<GoogleFeedItem>() {
    @field:Attribute
    var version: String? = null

    @field:Path("channel")
    @field:ElementList(name = "item", entry = "item", type = GoogleFeedItem::class, required = true, inline = true)
    override var feedItems: List<GoogleFeedItem>? = null
}

open class FeedItem : Comparable<FeedItem> {

    companion object {
        private val sharedRssDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
    }

    open var pubDate: String? = ""

    open var title: String? = ""

    open var link: String? = ""

    open var description: String? = ""

    open var image: String? = ""

    open var source: String = ""

    override fun compareTo(other: FeedItem): Int {
        return try {
            val formatter = sharedRssDateFormat
            val o1Date = formatter.parse(this.pubDate)
            val o2Date = formatter.parse(other.pubDate)
            if (o1Date.before(o2Date)) {
                1
            } else {
                -1
            }
        } catch (e: ParseException) {
            1
        }
    }
}

@Root(name = "item", strict = false)
class LiputanFeedItem @JvmOverloads constructor(

        @field:Element
        override var pubDate: String? = "",

        @field:Path("title")
        @field:Text(data = true)
        override var title: String? = "",

        @field:Element
        override var link: String? = "",

        @field:Path("description")
        @field:Text(data = true)
        override var description: String? = "",

        @field:Path("media:thumbnail")
        @field:Attribute(name = "url", required = false)
        override var image: String? = "",

        override var source: String = "liputan6"
) : FeedItem()


@Root(name = "item", strict = false)
class GoogleFeedItem @JvmOverloads constructor(

        @field:Element
        override var pubDate: String? = "",

        @field:Path("title")
        @field:Text(data = true)
        override var title: String? = "",

        @field:Element
        override var link: String? = "",

        @field:Path("description")
        @field:Text(data = true)
        override var description: String? = "",

        @field:Path("media:content")
        @field:Attribute(name = "url", required = false)
        override var image: String? = null,

        @field:Element(name = "source")
        override var source: String = ""
) : FeedItem()


@Root(name = "item", strict = false)
class DetikFeedItem @JvmOverloads constructor(

        @field:Element
        override var pubDate: String? = "",

        @field:Path("title")
        @field:Text(data = true)
        override var title: String? = "",

        @field:Element
        override var link: String? = "",

        @field:Path("description")
        @field:Text(data = true)
        override var description: String? = "",

        @field:Path("enclosure")
        @field:Attribute(name = "url", required = false)
        override var image: String? = "",

        override var source: String = "Detik"
) : FeedItem()

