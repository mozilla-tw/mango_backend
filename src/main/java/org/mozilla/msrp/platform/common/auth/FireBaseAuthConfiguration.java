package org.mozilla.msrp.platform.common.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Inject;

@Configuration
public class FireBaseAuthConfiguration implements WebMvcConfigurer {

    private FirebaseAuthInterceptor firebaseAuthInterceptor;
    private AdministratorAuthInterceptor administratorAuthInterceptor;


    @Inject
    public FireBaseAuthConfiguration(FirebaseAuthInterceptor firebaseAuthInterceptor,
                                     AdministratorAuthInterceptor administratorAuthInterceptor) {
        this.firebaseAuthInterceptor = firebaseAuthInterceptor;
        this.administratorAuthInterceptor = administratorAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.firebaseAuthInterceptor)
                .addPathPatterns("/api/v1/redeem/**",
                        "/api/v1/missions/**",
                        "/api/v1/group/**",
                        "/api/v1/ping/**");

        registry.addInterceptor(this.administratorAuthInterceptor)
                .addPathPatterns("/api/v1/admin/**");

    }
}