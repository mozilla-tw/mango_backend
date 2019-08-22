package org.mozilla.msrp.platform.profile;

import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@RestController
public class ProfileController {
    private ProfileRepository profileRepository;
    private FirefoxAccountService firefoxAccountService;

    @Inject
    public ProfileController(ProfileRepository repo, FirefoxAccountService service) {
        profileRepository = repo;
        firefoxAccountService = service;
    }

    @RequestMapping("/done")
    String done(@RequestParam(value = "jwt") String jwt, @RequestParam(value = "fxaAccessToken") String fxaAccessToken) {
        if (jwt == null || jwt.length() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GWT generation failed");
        }
        return "Debug:jwt[" + jwt + "]  [" + fxaAccessToken +"], closing the webview";
    }

    @RequestMapping("/login")
    String login(@RequestParam(value = "code") String code,
                 @RequestParam(value = "state") String oldFbUid,
                 HttpServletResponse httpResponse) {

        try {
            String fxaAccessToken = firefoxAccountService.authorization(code);

            FxAProfileResponse profileResponse = firefoxAccountService.profile(fxaAccessToken);

            String fxUid = profileResponse.getUid();
            String fxEmail = profileResponse.getEmail();

            profileRepository.signInAndUpdateUserDocument(oldFbUid, fxUid, fxEmail);

            // create custom token (jwt) for Firebase client SDK
            HashMap<String, String> additionalClaims = new HashMap<>();
            additionalClaims.put("fxuid", fxUid);
            additionalClaims.put("oldFbUid", oldFbUid);
            String customToken = profileRepository.createCustomToken(fxUid, additionalClaims);
            // We don't really need this info. Just to let client intercept the url and close the webview.
            // TODO: remove below debugging information
            httpResponse.sendRedirect("/done?jwt=" + customToken + "&fxaAccessToken=" + fxaAccessToken);

            return "done";

        } catch (IOException | JSONException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
        }
    }
}
