package io.pivotal.cf.dh;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@Configuration
public class Config {

    @Bean
    public KeyPairGenerator KeyPairGenerator() throws NoSuchAlgorithmException {
        return KeyPairGenerator.getInstance("DH");
    }

    @Bean
    public KeyFactory keyFactory() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance("DH");
    }
}