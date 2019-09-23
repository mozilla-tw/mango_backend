package org.mozilla.msrp.platform.vertical.news


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.springframework.context.annotation.Bean
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Named

interface DetikRssFeedClient {

    @GET("index.php/{topic}")
    fun rss(@Path("topic") topic: String): Call<DetikRss>

}

@Named
class DetikRssFeedClientConfig {

    @Bean
    fun DetikRssFeedClientFactory(): DetikRssFeedClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
            .baseUrl("http://rss.detik.com/")
            .client(client)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(DetikRssFeedClient::class.java)
    }
}