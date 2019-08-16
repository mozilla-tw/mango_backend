package org.mozilla.msrp.platform.auth;

import org.springframework.stereotype.Service;

@Service
interface FirefoxAccountService {
    String authorization(String code);

    String verify(String fxToken);

    String profile(String bearer);

}
