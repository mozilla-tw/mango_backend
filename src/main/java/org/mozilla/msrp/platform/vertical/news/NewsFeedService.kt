package org.mozilla.msrp.platform.vertical.news

import java.text.ParseException
import java.text.SimpleDateFormat
import javax.inject.Inject
import javax.inject.Named

@Named
class GoogleNewsFeedService @Inject constructor(private val googleRssFeedRepository: GoogleRssFeedRepository) {

    fun getNews(language: String) = googleRssFeedRepository.news(language)

    fun getNews(topic: String, hl: String, gl: String, ceid: String) = googleRssFeedRepository.news(topic, hl, gl, ceid)

}


@Named
class IndonesiaNewsFeedService @Inject constructor(
        private val liputan6RssFeedRepository: Liputan6RssFeedRepository,
        private val detikRssFeedRepository: DetikRssFeedRepository) {

    fun getNews(liputanTopicId: String, detikTopicId: String?): List<FeedItem>? {
        val liputan6List = liputan6RssFeedRepository.news(liputanTopicId)?: emptyList()

        val detikList = detikTopicId?.let {
            detikRssFeedRepository.news(it)
        }?: emptyList()

        val allNewsList = liputan6List.toMutableList().apply {
            addAll(detikList)
        }

        allNewsList.sortWith(Comparator<FeedItem> { o1, o2 ->
            val grater = o1?.let {
                try {
                    val formatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
                    val o1Date = formatter.parse(it.pubDate)
                    val o2Date = formatter.parse(o2.pubDate)
                    o1Date.before(o2Date)
                } catch (e: ParseException) {
                    false
                }
            } ?: false
            if (grater) {
                1
            } else {
                -1
            }
        })

        return allNewsList
    }
}



