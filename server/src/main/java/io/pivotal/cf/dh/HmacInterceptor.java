package io.pivotal.cf.dh;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;

@Component
class HmacInterceptor implements HandlerInterceptor {

    @Autowired
    private Util util;

    @Autowired
    private Server server;

    private Party getParty(HttpServletRequest request) throws GeneralSecurityException {
        String name = util.getName(request);

        Party p = server.getParty(name);

        if (p == null) {
            throw new GeneralSecurityException("Party not found for name: " + name);
        }
        return p;
    }

    private void validate(Party party, HttpServletRequest request, String content) throws GeneralSecurityException {
        String token = getToken(request);
        String date = request.getHeader("date");
        String method = request.getMethod();
        String uri = request.getRequestURI();

        util.validate(party, token, date, method, uri, content);
    }

    private String getToken(HttpServletRequest request) throws GeneralSecurityException {
        return util.getAuth(request).split(":")[1];
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        System.out.println("pre &&&&&&&&&&&&&");

        //validate the request
        validate(getParty(httpServletRequest), httpServletRequest, null);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        System.out.println("post: ***************");

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        System.out.println("after: ##################");

    }
}