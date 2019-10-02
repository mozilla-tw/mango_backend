package org.mozilla.msrp.platform.vertical.news

import org.junit.Test
import org.simpleframework.xml.core.Persister
import java.io.File


class RssDataTest {

    @Test
    fun parseGoogle() {
        val serializer = Persister()
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("./rssGoogle.xml")!!.file)
        val rss: GoogleRss = read(serializer, file)
        assert(rss.feedItems?.filter { it.title != "" }?.size == 70)
        assert(rss.feedItems?.filter { it.image != "" }?.size == 36)
        assert(rss.feedItems?.filter { it.source != "" }?.size == 70)
        assert(rss.feedItems?.filter { it.description != "" }?.size == 70)
        assert(rss.feedItems?.filter { it.pubDate != "" }?.size == 70)
        assert(rss.feedItems?.filter { it.link != "" }?.size == 70)
    }

    @Test
    fun parseLiputan6() {
        val serializer = Persister()
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("./rssLiputan6.xml")!!.file)
        val rss: Liputan6Rss = read(serializer, file)
        assert(rss.feedItems?.filter { it.title != "" }?.size == 50)
        assert(rss.feedItems?.filter { it.image != "" }?.size == 50)
        assert(rss.feedItems?.filter { it.source != "" }?.size == 50)
        assert(rss.feedItems?.filter { it.description != "" }?.size == 50)
        assert(rss.feedItems?.filter { it.pubDate != "" }?.size == 50)
        assert(rss.feedItems?.filter { it.link != "" }?.size == 50)
    }

    @Test
    fun parseDetik() {
        val serializer = Persister()
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("./rssDetik.xml")!!.file)
        val rss: DetikRss = read(serializer, file)
        assert(rss.feedItems?.filter { it.title != "" }?.size == 9)
        assert(rss.feedItems?.filter { it.image != "" }?.size == 9)
        assert(rss.feedItems?.filter { it.source != "" }?.size == 9)
        assert(rss.feedItems?.filter { it.description != "" }?.size == 9)
        assert(rss.feedItems?.filter { it.pubDate != "" }?.size == 9)
        assert(rss.feedItems?.filter { it.link != "" }?.size == 9)
    }

}

inline fun <reified T : Any> read(serializer: Persister, file: File): T = serializer.read(T::class.java, file)
