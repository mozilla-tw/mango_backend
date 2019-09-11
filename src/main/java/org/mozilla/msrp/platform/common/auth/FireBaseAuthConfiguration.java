package org.mozilla.msrp.platform.common.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Inject;

@Configuration
public class FireBaseAuthConfiguration implements WebMvcConfigurer {

    private FirebaseAuthInterceptor firebaseAuthInterceptor;


    @Inject
    public FireBaseAuthConfiguration(FirebaseAuthInterceptor firebaseAuthInterceptor) {
        this.firebaseAuthInterceptor = firebaseAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.firebaseAuthInterceptor)
                .addPathPatterns("/api/v1/redeem/**",
                        "/api/v1/missions/**",
                        "/api/v1/group/**",
                        "/api/v1/ping/**");
    }
}