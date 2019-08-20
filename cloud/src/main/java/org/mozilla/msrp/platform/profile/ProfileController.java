package org.mozilla.msrp.platform.profile;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@RestController
public class ProfileController {
    private ProfileRepository profileRepository;
    private FirefoxAccountService firefoxAccountService;

    @Autowired
    public ProfileController(ProfileRepository repo, FirefoxAccountService service) {
        profileRepository = repo;
        firefoxAccountService = service;
    }

    @RequestMapping("/done")
    String done(@RequestParam(value = "jwt") String jwt) {
        return this.toString() + "jwt is here [" + jwt + "], closing the webview";
    }


    @RequestMapping("/login")
    String login(@RequestParam(value = "code") String code,
                 @RequestParam(value = "state") String oldFbUid,
                 HttpServletResponse httpResponse) throws IOException, JSONException {


        try {
            String fxToken = firefoxAccountService.authorization(code);

            String fxUid = firefoxAccountService.verify(fxToken);

            profileRepository.promoteUserDocument(oldFbUid, fxUid);

            // create custom token (jwt) for Firebase client SDK
            HashMap<String, String> additionalClaims = new HashMap<String, String>();
            additionalClaims.put("fxuid", fxUid);
            additionalClaims.put("oldFbUid", oldFbUid);
            String customToken = profileRepository.createCustomToken(fxUid, additionalClaims);
            httpResponse.sendRedirect("/done?jwt="+customToken+"&at=$fxToken&email=nechen@mozilla.com");

            return "done";

        } catch (Exception e) {
            return "exception===" + e.getLocalizedMessage();
        }
    }
}
