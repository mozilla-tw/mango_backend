package org.mozilla.msrp.platform.vertical.news

import javax.inject.Inject
import javax.inject.Named

@Named
class GoogleNewsFeedService @Inject constructor(private val googleRssFeedRepository: GoogleRssFeedRepository) {

    fun getNews(language: String) = googleRssFeedRepository.news(language)

    fun getTopNews(hl: String, gl: String, ceid: String): List<FeedItem>? {
        val list = googleRssFeedRepository.topNews(hl, gl, ceid)
        return list?.sorted()
    }

    fun getNews(topic: String, hl: String, gl: String, ceid: String): List<FeedItem>? {
        val list = googleRssFeedRepository.news(topic, hl, gl, ceid)
        return list?.sorted()
    }

}


@Named
class IndonesiaNewsFeedService @Inject constructor(
        private val liputan6RssFeedRepository: Liputan6RssFeedRepository,
        private val detikRssFeedRepository: DetikRssFeedRepository) {

    fun getNews(liputan6Url: String?, detikUrl: String?): List<FeedItem>? {

        val liputan6List = liputan6Url?.let {
            liputan6RssFeedRepository.news(it)
        } ?: emptyList()

        val detikList = detikUrl?.let {
            detikRssFeedRepository.news(it)
        }?: emptyList()

        val allNewsList = liputan6List.toMutableList().apply {
            addAll(detikList)
        }
        return allNewsList.sorted()
    }
}



