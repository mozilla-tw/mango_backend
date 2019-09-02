package org.mozilla.msrp.platform.profile;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mozilla.msrp.platform.profile.FirefoxAccountService.API_AUTH_RESPONSE_KEY_ACCESS_TOKEN;

@RunWith(SpringJUnit4ClassRunner.class)
public class FirefoxAccountServiceTest {

    @Test
    public void some_meaning_less_tes() {
        final String fakeToken = "fake";
        final String fakeTokenResponse = "{" +
                "\"" + API_AUTH_RESPONSE_KEY_ACCESS_TOKEN + "\":" + "\"" + fakeToken + "\"" +
                "}";
        FirefoxAccountService service = new FirefoxAccountService();
        Retrofit retrofit = mock(Retrofit.class);
        service.setRetrofit(retrofit);
        FirefoxAccountServiceInfo info = new FirefoxAccountServiceInfo("1", "secret", fakeToken, "profile");
        service.setFirefoxAccountServiceInfo(info);
        try {

            when(retrofit.load(Mockito.any())).thenReturn(fakeTokenResponse);
            assertEquals(fakeToken, service.authorization(""));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            fail();
        }
    }
}