package io.pivotal.cf.dh;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.GeneralSecurityException;

@Component
class WebSecurity {

    private static final Logger LOG = Logger.getLogger(WebSecurity.class);

    @Autowired
    private Util util;

    @Autowired
    private Server server;

    public boolean authenticate(Authentication authentication, HttpServletRequest request) {
        try {
            String token = util.getToken(request);
            String date = request.getHeader("date");
            String method = request.getMethod();
            String uri = request.getRequestURI();
            Party party = getParty(request);

            util.validate(party, token, date, method, uri, null);
        } catch (GeneralSecurityException e) {
            LOG.info("invalid hmac.", e);
            return false;
        }

        return true;
    }

    private Party getParty(HttpServletRequest request) throws GeneralSecurityException {
        String name = util.getName(request);

        Party p = server.getParty(name);

        if (p == null) {
            throw new GeneralSecurityException("Party not found for name: " + name);
        }
        return p;
    }
}