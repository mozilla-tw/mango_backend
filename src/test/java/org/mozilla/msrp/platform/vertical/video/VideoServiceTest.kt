package org.mozilla.msrp.platform.vertical.video

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class VideoServiceTest {


    lateinit var videoService: VideoService

    @Mock
    lateinit var youtubeClient: YoutubeClient

    @Mock
    lateinit var videoCacheRepository: VideoCacheRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        videoService = VideoService(youtubeClient, videoCacheRepository)
        videoService.mapper = ObjectMapper()
    }

    @Test
    fun `if we see this key for the first time, we should load data`() {
        Mockito.`when`(videoCacheRepository.get("KEY")).thenReturn(null)

        videoService.fromCache("KEY", "", 0L)

        verify(youtubeClient, times(1)).videoList("KEY", "", 0L)

    }

    @Test
    fun `if there is no video for this key, we should load data`() {
        val result = "{\"videos\":[],\"ts\":" + (System.currentTimeMillis() + 24 * 60 * 60 * 1000) + "}"

        Mockito.`when`(videoCacheRepository.get("KEY")).thenReturn(result)

        videoService.fromCache("KEY", "", 0L)

        verify(youtubeClient, times(1)).videoList("KEY", "", 0L)

    }

    @Test
    fun `if the data for this key is expired, we should load data`() {
        val result = "{\"videos\":[],\"ts\":" + (System.currentTimeMillis() - 24 * 60 * 60 * 1000) + "}"

        Mockito.`when`(videoCacheRepository.get("KEY")).thenReturn(result)

        videoService.fromCache("KEY", "", 0L)

        verify(youtubeClient, times(1)).videoList("KEY", "", 0L)
    }

    @Test
    fun `if the data for this key is not expired and is not empty, we use the cached data`() {
        val result = "{\"videos\":[{\"title\":\"【Eng Sub】泰國：曼谷 Travel Vlog 第1集：2018年最新美食攻略 | Stormscape\",\"channelTitle\":\"StormScape\",\"publishedAt\":\"2018-08-29T12:00:01.000Z\",\"thumbnail\":\"https://i.ytimg.com/vi/3aBOtWsT3QU/mqdefault.jpg\",\"duration\":\"08:37\",\"link\":\"https://www.youtube.com/watch?v=3aBOtWsT3QU\",\"viewCount\":\"173973\",\"componentId\":\"14a884f201f6c7baf0512a98b5d3fe046d9dc219596b0bea82d7ac11062be167\",\"source\":\"youtube\"},{\"title\":\"Spice 泰國 | 曼谷不為人知的私房玩法：米其林一星路邊攤、海灘秘境、超時尚咖啡廳、現代風格夜市：泰國 自由行 2019\",\"channelTitle\":\"Spice Travel\",\"publishedAt\":\"2018-03-02T16:06:26.000Z\",\"thumbnail\":\"https://i.ytimg.com/vi/dur7IeG8VtY/mqdefault.jpg\",\"duration\":\"09:39\",\"link\":\"https://www.youtube.com/watch?v=dur7IeG8VtY\",\"viewCount\":\"171976\",\"componentId\":\"39a8ec41d06216fa302452950cc774a758dcb607c14b747f2fb240529207e7df\",\"source\":\"youtube\"},{\"title\":\"【Eng Sub】泰國：曼谷 Travel Vlog 第2集：2018年美食攻略續集 | Stormscape\",\"channelTitle\":\"StormScape\",\"publishedAt\":\"2018-09-05T12:00:13.000Z\",\"thumbnail\":\"https://i.ytimg.com/vi/unAmfWP5j_w/mqdefault.jpg\",\"duration\":\"08:37\",\"link\":\"https://www.youtube.com/watch?v=unAmfWP5j_w\",\"viewCount\":\"76855\",\"componentId\":\"ae79eb41a8499d9b3cf0f036bb89c7665aa6cbbe14e448f692177f086ac50fe9\",\"source\":\"youtube\"},{\"title\":\"Spice 泰國 | 米其林路邊小吃 ?! 曼谷人私藏的道地美食攻略：泰國 自由行 必吃\",\"channelTitle\":\"Spice Travel\",\"publishedAt\":\"2018-03-09T13:00:03.000Z\",\"thumbnail\":\"https://i.ytimg.com/vi/rJsJ0XGiigE/mqdefault.jpg\",\"duration\":\"07:58\",\"link\":\"https://www.youtube.com/watch?v=rJsJ0XGiigE\",\"viewCount\":\"82638\",\"componentId\":\"248a63f09677c454e75d8b20784ab249188c2213377b79b3bc6c624abb262e77\",\"source\":\"youtube\"},{\"title\":\"食盡曼谷TRAVEL VLOG | Bangkok | #travelwithmins\",\"channelTitle\":\"Just Minnie\",\"publishedAt\":\"2018-07-22T10:10:55.000Z\",\"thumbnail\":\"https://i.ytimg.com/vi/a-ayY93FSa0/mqdefault.jpg\",\"duration\":\"13:50\",\"link\":\"https://www.youtube.com/watch?v=a-ayY93FSa0\",\"viewCount\":\"42934\",\"componentId\":\"b7a3eaad71124e2663c92550ad9672a4281058a1d1084005886082fdb21f0a53\",\"source\":\"youtube\"}],\"ts\":"+System.currentTimeMillis()+"}"

        Mockito.`when`(videoCacheRepository.get("KEY")).thenReturn(result)

        videoService.fromCache("KEY", "", 0L)

        verify(youtubeClient, times(0)).videoList("KEY", "", 0L)
    }
}