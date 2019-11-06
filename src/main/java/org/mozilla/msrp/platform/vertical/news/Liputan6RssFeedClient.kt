package org.mozilla.msrp.platform.vertical.news


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.springframework.context.annotation.Bean
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Named

interface Liputan6RssFeedClient {

    @GET("mozilla?source=Digital%20Marketing&medium=Partnership")
    fun rss(@Query("categories[]") topic: String): Call<Liputan6Rss>
}

@Named
class Liputan6RssFeedClientConfig {

    @Bean
    fun Liputan6RssFeedClientFactory(): Liputan6RssFeedClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
                .baseUrl(System.getProperty("news.rss-liputan6-api"))
                .client(client)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build()
                .create(Liputan6RssFeedClient::class.java)
    }
}