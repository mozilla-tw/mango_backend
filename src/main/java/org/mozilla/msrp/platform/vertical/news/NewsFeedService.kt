package org.mozilla.msrp.platform.vertical.news

import javax.inject.Inject
import javax.inject.Named

@Named
class GoogleNewsFeedService @Inject constructor(private val googleRssFeedRepository: GoogleRssFeedRepository) {

    fun getNews(language: String) = googleRssFeedRepository.news(language)

    fun getNews(language: String, topic: String) = googleRssFeedRepository.news(language, topic)

}


@Named
class IndonesiaNewsFeedService @Inject constructor(
        private val liputan6RssFeedRepository: Liputan6RssFeedRepository,
        private val detikRssFeedRepository: DetikRssFeedRepository) {

    fun getNews(liputanTopicId: String, detikTopicId: String?): List<FeedItem>? {
        val liputan6List = liputan6RssFeedRepository.news(liputanTopicId)
        var detikList: List<FeedItem>? = null
        if (detikTopicId != null) {
            detikList = detikRssFeedRepository.news(detikTopicId)
        }

        return liputan6List?.toMutableList()?.apply {
            addAll(detikList?.toList() ?: listOf())
        }
    }
}



