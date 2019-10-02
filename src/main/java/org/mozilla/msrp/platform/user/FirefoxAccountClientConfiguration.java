package org.mozilla.msrp.platform.user;

import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.inject.Named;

@Log4j2
@Named
public class FirefoxAccountClientConfiguration {


    @Bean("FxaAuth")
    public FirefoxAccountClient authFirefoxAccountClient() {
        log.info(" --- Bean Creation FxaAuth ---");

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


        return new Retrofit.Builder()
                .baseUrl("https://oauth-stable.dev.lcip.org")
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(FirefoxAccountClient.class);
    }


    @Bean("FxaProfile")
    public FirefoxAccountClient profileFirefoxAccountClientFactory() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


        return new Retrofit.Builder()
                .baseUrl("https://stable.dev.lcip.org/profile/")
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build().create(FirefoxAccountClient.class);
    }
}
