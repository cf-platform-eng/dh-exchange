package io.pivotal.cf.dh;

import feign.Feign;
import feign.gson.GsonDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.security.*;

@Configuration
@EnableWebSecurity
class Config extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/server/quote/**").access("@webSecurity.authenticate(authentication,request)");
    }

    @Bean
    public KeyPairGenerator keyPairGenerator() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(Util.ELLIPTIC_KEY_TYPE);
        kpg.initialize(Util.KEY_SIZE);
        return kpg;
    }

    @Bean
    public KeyFactory keyFactory() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance(Util.ELLIPTIC_KEY_TYPE);
    }

    @Bean
    public QuoteRepository quoteRepository() {
        return Feign
                .builder()
                .decoder(new GsonDecoder())
                .target(QuoteRepository.class,
                        "https://query.yahooapis.com/v1/public");
    }

    @Bean
    Party bob() throws NoSuchAlgorithmException, InvalidKeyException {
        return new Party("bob");
    }
}