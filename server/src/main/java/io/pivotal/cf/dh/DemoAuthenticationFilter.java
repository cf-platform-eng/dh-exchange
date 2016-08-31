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

@Component
class DemoAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private DemoAuthenticationProvider demoAuthenticationProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String s = request.getHeader("Authorization");

        if (s == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String auth[] = s.split(":");
        Authentication authentication = new UsernamePasswordAuthenticationToken(auth[0], auth[1]);
        Authentication successfulAuthentication = demoAuthenticationProvider.authenticate(authentication);

        SecurityContextHolder.getContext().setAuthentication(successfulAuthentication);
        filterChain.doFilter(request, response);
    }
}