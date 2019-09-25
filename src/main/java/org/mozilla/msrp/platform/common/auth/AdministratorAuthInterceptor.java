package org.mozilla.msrp.platform.common.auth;

import com.google.firebase.auth.FirebaseToken;
import lombok.extern.log4j.Log4j2;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Log4j2
@Named
public class AdministratorAuthInterceptor extends FirebaseAuthInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return super.preHandle(request, response, handler);
    }

    @Override
    void setRequestAttribute(HttpServletRequest request, FirebaseToken decodedToken) {
        super.setRequestAttribute(request, decodedToken);
        request.setAttribute("admin", isUserAdmin(decodedToken));
    }

    private boolean isUserAdmin(FirebaseToken decodedToken) {
        Object role = decodedToken.getClaims().getOrDefault("role", "");
        if (role instanceof String) {
            String roleString = (String) role;
            return "admin".equals(roleString);
        }
        return false;
    }

}