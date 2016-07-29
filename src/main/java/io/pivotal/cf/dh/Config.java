package io.pivotal.cf.dh;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.security.spec.InvalidParameterSpecException;

@Configuration
public class Config {

    @Bean
    public KeyPairGenerator keyPairGenerator(DHParameterSpec dhSkipParamSpec) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
        kpg.initialize(dhSkipParamSpec);
        return kpg;
    }

    @Bean
    public KeyFactory keyFactory() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance("DH");
    }

    @Bean
    DHParameterSpec dhSkipParamSpec() throws NoSuchAlgorithmException, InvalidParameterSpecException {
        AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
        paramGen.init(512);
        AlgorithmParameters params = paramGen.generateParameters();
        return params.getParameterSpec(DHParameterSpec.class);
    }

    @Bean
    Party alice(KeyPairGenerator keyPairGenerator, KeyFactory keyFactory) throws NoSuchAlgorithmException, InvalidKeyException {
        return new Party(keyPairGenerator, keyFactory);
    }

    @Bean
    Party bob(KeyPairGenerator keyPairGenerator, KeyFactory keyFactory) throws NoSuchAlgorithmException, InvalidKeyException {
        return new Party(keyPairGenerator, keyFactory);
    }
}