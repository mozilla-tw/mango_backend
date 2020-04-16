package org.mozilla.msrp.platform.push.service

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.mozilla.msrp.platform.push.model.StmoResponse
import org.springframework.context.annotation.Bean
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import javax.inject.Named

interface StmoClient {

    @GET
    fun fromUrl(@Url url: String): Call<StmoResponse>
}

@Named
class StmoClientConfiguration {

    @Bean
    fun provideStmoClient(): StmoClient {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.HEADERS
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
                .baseUrl("https://google.com")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(Gson())).build()
                .create(StmoClient::class.java)
    }
}