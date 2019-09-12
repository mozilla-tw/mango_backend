package org.mozilla.msrp.platform.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.log4j.Log4j2;
import org.mozilla.msrp.platform.common.ErrorMessage;
import org.mozilla.msrp.platform.profile.ProfileRepository;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@Log4j2
@Named
public class FirebaseAuthInterceptor implements HandlerInterceptor {

    private static final String HEADER_BEAR = "Bearer ";

    @Inject
    ObjectMapper mapper;

    @Inject
    ProfileRepository profileRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.contains(HEADER_BEAR)) {

            String jwt = authorization.replace(HEADER_BEAR, "");

            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(jwt);

                if (decodedToken.getUid().isEmpty()) {

                    handleThrowable(response, HttpStatus.UNAUTHORIZED, "No such user");

                    return false;

                } else {
                    request.setAttribute("uid", getUserId(decodedToken));
                    return true;
                }

            } catch (IllegalArgumentException e) {
                logThrowable("illegal token format", e);
                handleThrowable(response, HttpStatus.BAD_REQUEST, "Illegal token format");
                return false;

            } catch (FirebaseAuthException e) {
                logThrowable("unauthorized token", e);
                handleThrowable(response, HttpStatus.UNAUTHORIZED, "Unauthorized token");
                return false;

            } catch (Throwable throwable) {
                logThrowable("unexpected exception", throwable);
                handleThrowable(response, HttpStatus.INTERNAL_SERVER_ERROR, "Error loading DB");

                return false;
            }

        } else {

            log.info("abnormal access to endpoint: {}", request.getRequestURI());

            handleThrowable(response, HttpStatus.UNAUTHORIZED, "Please login first");

            return false;
        }
    }

    private void handleThrowable(HttpServletResponse response, HttpStatus httpStatus, String message) throws IOException {
        ErrorMessage errorObj = new ErrorMessage(new Date(), message);
        String jsonMessage = mapper.writeValueAsString(errorObj);
        response.addHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);
        response.setStatus(httpStatus.value());
        PrintWriter writer = response.getWriter();
        writer.write(jsonMessage);
        writer.flush();
        writer.close();
    }

    @Nullable
    private String getUserId(FirebaseToken token) {
        String fbuid = token.getUid();
        String fxuid = (String) token.getClaims().getOrDefault("fxuid", "");
        return profileRepository.findUserId(fbuid, fxuid);
    }

    private void logThrowable(String msg, Throwable throwable) {
        String message = NestedExceptionUtils.buildMessage(msg, throwable);
        log.info("authenticate failed, msg={}", message);
    }
}