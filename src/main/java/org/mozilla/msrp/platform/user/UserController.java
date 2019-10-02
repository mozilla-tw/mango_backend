package org.mozilla.msrp.platform.user;

import org.json.JSONException;
import org.mozilla.msrp.platform.common.auth.JwtHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

import static org.mozilla.msrp.platform.common.auth.JwtHelper.ROLE_MSRP_ADMIN;
import static org.mozilla.msrp.platform.common.auth.JwtHelper.ROLE_PUBLISH_ADMIN;


@RestController
public class UserController {
    private UserRepository userRepository;
    private FirefoxAccountService firefoxAccountService;

    @Inject
    public UserController(UserRepository repo, FirefoxAccountService service) {
        userRepository = repo;
        firefoxAccountService = service;
    }

    @RequestMapping("/api/v1/done")
    String done(@RequestParam(value = "jwt") String jwt, @RequestParam(value = "fxaAccessToken") String fxaAccessToken) {
        if (jwt == null || jwt.length() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GWT generation failed");
        }
        return "Debug:jwt[" + jwt + "]  [" + fxaAccessToken + "], closing the webview";
    }

    @RequestMapping("/api/v1/login")
    ResponseEntity<LoginResponse> login(@RequestParam(value = "code") String code,
                                        @RequestParam(value = "state") String state,
                                        HttpServletResponse httpResponse) {    // need HttpServletResponse to redirect

        try {
            FxaTokenRequest fxaTokenRequest = firefoxAccountService.genFxaTokenRequest(code);
            String fxaAccessToken = firefoxAccountService.token(fxaTokenRequest);
            if (fxaAccessToken == null) {
                return new ResponseEntity<>(new LoginResponse.Fail("error in Fxa token api"), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            FxaProfileResponse profileResponse = firefoxAccountService.profile("Bearer " + fxaAccessToken);

            if (profileResponse == null) {
                return new ResponseEntity<>(new LoginResponse.Fail("error in Fxa token api"), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String fxUid = profileResponse.getUid();
            String fxEmail = profileResponse.getEmail();

            if (fxUid == null || fxEmail == null) {
                return new ResponseEntity<>(new LoginResponse.Fail("error in Fxa token api"), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (ROLE_PUBLISH_ADMIN.equals(state) && userRepository.isPublishAdmin(fxEmail)) {
                String token = JwtHelper.createToken(ROLE_PUBLISH_ADMIN);
                httpResponse.sendRedirect("/api/v1/admin/publish?token=" + token);
                return new ResponseEntity<>(HttpStatus.PERMANENT_REDIRECT);
            }

            if (ROLE_MSRP_ADMIN.equals(state) && userRepository.isMsrpAdmin(fxEmail)) {
                String token = JwtHelper.createToken(ROLE_MSRP_ADMIN);
                httpResponse.sendRedirect("/api/v1/admin/msrp?token=" + token);
                return new ResponseEntity<>(HttpStatus.PERMANENT_REDIRECT);
            }
            String oldFbUid = state;
            LoginResponse loginResponse = userRepository.signInAndUpdateUserDocument(oldFbUid, fxUid, fxEmail);
            if (loginResponse instanceof LoginResponse.Success) {
                HashMap<String, String> additionalClaims = new HashMap<>();
                additionalClaims.put("fxuid", fxUid);
                additionalClaims.put("oldFbUid", oldFbUid);
                String customToken = userRepository.createCustomToken(oldFbUid, additionalClaims);
                // We don't really need this info. Just to let client intercept the url and close the webview.
                // TODO: remove below debugging information
                httpResponse.sendRedirect("/api/v1/done?jwt=" + customToken + "&fxaAccessToken=" + fxaAccessToken);
                return new ResponseEntity<>(HttpStatus.PERMANENT_REDIRECT);
            } else if (loginResponse instanceof LoginResponse.SuspiciousWarning) {
                return new ResponseEntity<>(loginResponse, HttpStatus.PARTIAL_CONTENT);
            } else if (loginResponse instanceof LoginResponse.UserSuspended) {
                return new ResponseEntity<>(loginResponse, HttpStatus.UNAUTHORIZED);
            }

            return new ResponseEntity<>(loginResponse, HttpStatus.BAD_REQUEST);

        } catch (IOException | JSONException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
        }
    }
}
