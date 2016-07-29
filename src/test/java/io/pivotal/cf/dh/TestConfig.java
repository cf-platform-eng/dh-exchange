package io.pivotal.cf.dh;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@Configuration
public class TestConfig {

    @Bean
    Party tester(KeyPairGenerator keyPairGenerator, KeyFactory keyFactory) throws NoSuchAlgorithmException, InvalidKeyException {
        return new Party(keyPairGenerator, keyFactory);
    }
}