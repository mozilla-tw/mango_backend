package org.mozilla.msrp.platform.firestore;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Inject;

@Configuration
public class FirestoreConfiguration implements WebMvcConfigurer {

    private FirestoreInterceptor interceptor;

    @Inject
    public FirestoreConfiguration(FirestoreInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.interceptor)
                .addPathPatterns("/api/v1/**");
    }
}
