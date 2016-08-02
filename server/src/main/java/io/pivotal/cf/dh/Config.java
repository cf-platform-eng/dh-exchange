package io.pivotal.cf.dh;

import feign.Feign;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.security.*;

@Configuration
class Config {

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

//    @Bean
//    Party alice() throws NoSuchAlgorithmException, InvalidKeyException {
//        return new Party("alice");
//    }

    @Bean
    Party bob() throws NoSuchAlgorithmException, InvalidKeyException {
        return new Party("bob");
    }

    @Bean
    HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Bean
    public QuoteRepository quoteRepository() {
        return Feign
                .builder()
                .target(QuoteRepository.class,
                        "https://query.yahooapis.com/v1/public");
    }

    //TODO fix uris
    @Bean
    public ClientRepository clientRepository() {
        return Feign
                .builder()
                .target(ClientRepository.class,
                        "http://localhost:8080/client");
    }
}