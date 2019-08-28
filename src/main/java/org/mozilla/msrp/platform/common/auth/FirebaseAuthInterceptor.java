package org.mozilla.msrp.platform.common.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Log4j2
public class FirebaseAuthInterceptor implements HandlerInterceptor {

    private static String HEADER_BEAR = "Bear ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.contains(HEADER_BEAR)) {
            String jwt = authorization.replace(HEADER_BEAR, "");
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(jwt);
                if (decodedToken.getUid().isEmpty()) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        } else {
            log.info("abnormal access to endpoint: {}", request.getRequestURI());
        }
        return false;
    }

}