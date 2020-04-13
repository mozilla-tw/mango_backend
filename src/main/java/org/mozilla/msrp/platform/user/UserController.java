package org.mozilla.msrp.platform.user;

import static org.mozilla.msrp.platform.common.auth.JwtHelper.ROLE_MSRP_ADMIN;
import static org.mozilla.msrp.platform.common.auth.JwtHelper.ROLE_PUBLISH_ADMIN;
import static org.mozilla.msrp.platform.common.auth.JwtHelper.ROLE_PUSH_ADMIN;

import java.io.IOException;
import java.util.HashMap;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.mozilla.msrp.platform.common.auth.JwtHelper;
import org.mozilla.msrp.platform.metrics.Metrics;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
public class UserController {
    private UserRepository userRepository;
    private FirefoxAccountService firefoxAccountService;

    @Inject
    public UserController(UserRepository repo, FirefoxAccountService service) {
        userRepository = repo;
        firefoxAccountService = service;
    }
    @Inject
    private JwtHelper jwtHelper;

    // when the client sees this API, close the WebView
    @GetMapping("/api/v1/done")
    void done() {
    }

    @PostMapping("/api/v1/user/token")
    void updateToken(
            @RequestParam(value = "telemetry_client_id") String telemetryClientId,
            @RequestParam(value = "fcm_token") String fcmToken,
            @RequestAttribute("uid") String uid) {

        // write the data to User Service DB
        userRepository.updateFcmToken(uid, telemetryClientId, fcmToken);
        log.info("[updateToken][/api/v1/user/token][" + uid + "] ");
    }

    @GetMapping("/api/v1/login")
    void login(@RequestParam(value = "code") String code,
               @RequestParam(value = "state") String state,
               HttpServletResponse httpResponse) {    // need HttpServletResponse to redirect

        log.info("[login][/api/v1/login][" + state + "] ");

        try {
            FxaTokenRequest fxaTokenRequest = firefoxAccountService.genFxaTokenRequest(code);
            String fxaAccessToken = firefoxAccountService.token(fxaTokenRequest);
            if (fxaAccessToken == null) {
                log.error("[login][" + state + "] Fxa token api error");
                httpResponse.sendRedirect("/api/v1/done?login_success=false&msg=fxa_token_api");
                return;
            }

            FxaProfileResponse profileResponse = firefoxAccountService.profile("Bearer " + fxaAccessToken);

            if (profileResponse == null) {
                log.error("[login][" + state + "] Fxa profile api error");
                httpResponse.sendRedirect("/api/v1/done?login_success=false&msg=fxa_profile_api");
                return;
            }

            String fxUid = profileResponse.getUid();
            String fxEmail = profileResponse.getEmail();
            log.info("[login] User[" + fxUid + "] login with email:" + fxEmail);
            if (fxUid == null || fxEmail == null) {
                log.error("[login][" + state + "] No such user in fxa");
                httpResponse.sendRedirect("/api/v1/done?login_success=false&msg=no_Fxa_user");
                return;
            }

            if (ROLE_PUBLISH_ADMIN.equals(state) && userRepository.isPublishAdmin(fxEmail)) {
                String token = jwtHelper.createToken(ROLE_PUBLISH_ADMIN, fxEmail);
                log.info("[login][" + state + "] ROLE_PUBLISH_ADMIN");
                httpResponse.sendRedirect("/api/v1/admin/publish?token=" + token);
                return;
            }

            if (ROLE_MSRP_ADMIN.equals(state) && userRepository.isMsrpAdmin(fxEmail)) {
                String token = jwtHelper.createToken(ROLE_MSRP_ADMIN, fxEmail);
                log.info("[login][" + state + "] ROLE_PUBLISH_ADMIN");
                httpResponse.sendRedirect("/api/v1/admin/msrp?token=" + token);
                return;
            }

            if (ROLE_PUSH_ADMIN.equals(state) && userRepository.isPushAdmin(fxEmail)) {
                String token = jwtHelper.createToken(ROLE_MSRP_ADMIN, fxEmail);
                log.info("[login][" + state + "] ROLE_PUBLISH_ADMIN");
                httpResponse.sendRedirect("/api/v1/admin/push?token=" + token);
                return;
            }

            String oldFbUid = state;
            LoginResponse loginResponse = userRepository.signInAndUpdateUserDocument(oldFbUid, fxUid, fxEmail);

            HashMap<String, String> additionalClaims = new HashMap<>();
            additionalClaims.put("fxuid", fxUid);
            additionalClaims.put("oldFbUid", oldFbUid);
            String customToken = userRepository.createCustomToken(oldFbUid, additionalClaims);
            // log are handled in the repository
            if (loginResponse instanceof LoginResponse.Success) {
                log.info("[login]bind FxA success: login for the fist time this week");
                httpResponse.sendRedirect("/api/v1/done?jwt=" + customToken + "&login_success=true&disabled=false&times=0");
            } else if (loginResponse instanceof LoginResponse.FirstWarning) {
                log.info("[login]bind success: login for the second time this week");
                httpResponse.sendRedirect("/api/v1/done?jwt=" + customToken + "&login_success=true&disabled=false&times=1");
            } else if (loginResponse instanceof LoginResponse.SecondWarning) {
                log.info("[login]bind success: login for the three times  this week");
                httpResponse.sendRedirect("/api/v1/done?jwt=" + customToken + "&login_success=true&disabled=false&times=2");
            } else if (loginResponse instanceof LoginResponse.UserSuspended) {
                log.info("[login]bind success: login more than three times. User suspended");
                httpResponse.sendRedirect("/api/v1/done?jwt=" + customToken + "&login_success=true&disabled=true&times=3");
            } else if (loginResponse instanceof LoginResponse.Fail) {
                final String message = ((LoginResponse.Fail) loginResponse).getMessage();
                Metrics.event(Metrics.EVENT_USER_BIND_FAIL, message);
                log.info("[login]bind fail:" + message);
                httpResponse.sendRedirect("/api/v1/done?login_success=false");
            }

        } catch (IOException | JSONException e) {
            Metrics.event(Metrics.EVENT_USER_BIND_FAIL, e.toString());
            log.error("[login][" + state + "] Exception:" + e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
        }
    }
}
