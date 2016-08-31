package io.pivotal.cf.dh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@Component
class HmacAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private Util util;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String name = util.getName(request);
            String token = util.getToken(request);

            String date = request.getHeader("date");
            String method = request.getMethod();
            String uri = request.getRequestURI();

            String sig = util.signThis(date, method, uri, null);
            Map<String, String> creds = new HashMap<>();
            creds.put("hmac", token);
            creds.put("signature", sig);

            Authentication authentication = new UsernamePasswordAuthenticationToken(name, creds);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (GeneralSecurityException e) {
            //ignore, no token on this request
        }
        filterChain.doFilter(request, response);
    }
}