package io.pivotal.cf.dh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
class HmacAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private Server server;

    @Autowired
    private Util util;

    @Override
    @SuppressWarnings(value = "unchecked")
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String name = authentication.getName();

        Party party = server.getParty(name);
        if (party == null) {
            throw new UsernameNotFoundException("user: " + name + " not found.");
        }

        Map<String, String> creds;

        try {
            creds = (Map<String, String>) authentication.getCredentials();
        } catch (ClassCastException e) {
            throw new BadCredentialsException("invalid credentials format.");
        }

        String hmac = creds.get("hmac");
        String signature = creds.get("signature");

        if (hmac == null || signature == null) {
            throw new BadCredentialsException("incomplete credentials.");
        }

        try {
            util.validate(party, hmac, signature);
        } catch (GeneralSecurityException e) {
            throw new BadCredentialsException("invalid hmac token.");
        }

        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new UsernamePasswordAuthenticationToken(name, hmac, grantedAuths);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}