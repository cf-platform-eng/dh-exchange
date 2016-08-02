package io.pivotal.cf.dh;

import feign.Feign;
import io.pivotal.cf.dh.service.ClientRepository;
import io.pivotal.cf.dh.service.QuoteRepository;
import io.pivotal.cf.dh.service.ServerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.security.*;

@Configuration
class Config {

    /**
     * https://www.rfc-editor.org/rfc/rfc7748.txt
     */
    static final String ELLIPTIC_KEY_TYPE = "EC";

    //    static final String DH_KEY_TYPE = "DH";
    static final String ELLIPTIC_KEY_AGREEMENT_TYPE = "ECDH";
    static final String DIGEST_TYPE = "SHA-256";
    static final String CIPHER_TYPE = "AES";
    static final int KEY_SIZE = 256;

    @Bean
    public KeyPairGenerator keyPairGenerator() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(ELLIPTIC_KEY_TYPE);
        kpg.initialize(KEY_SIZE);
        return kpg;
    }

    @Bean
    public KeyFactory keyFactory() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance(ELLIPTIC_KEY_TYPE);
    }

    @Bean
    Party alice() throws NoSuchAlgorithmException, InvalidKeyException {
        return new Party("alice");
    }

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
    public ServerRepository serverRepository() {
        return Feign
                .builder()
                .target(ServerRepository.class,
                        "http://localhost:8080/server");
    }

    @Bean
    public ClientRepository clientRepository() {
        return Feign
                .builder()
                .target(ClientRepository.class,
                        "http://localhost:8080/client");
    }
}