package io.pivotal.cf.dh;

import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

abstract class Party {

    @Autowired
    private KeyPairGenerator keyPairGenerator;

    @Autowired
    private KeyFactory keyFactory;

    @Autowired
    private Util util;

    private KeyAgreement keyAgree;

    private SecretKey desKey;

    private KeyPair keyPair;

    private Cipher cbcCipher;

    KeyPairGenerator getKeyPairGenerator() {
        return keyPairGenerator;
    }

    KeyFactory getKeyFactory() {
        return keyFactory;
    }

    Util getUtil() {
        return util;
    }

    KeyAgreement getKeyAgree() {
        return keyAgree;
    }

    void setKeyAgree(KeyAgreement keyAgree) {
        this.keyAgree = keyAgree;
    }

    SecretKey getDesKey() {
        return desKey;
    }

    void setDesKey(SecretKey desKey) {
        this.desKey = desKey;
    }

    KeyPair getKeyPair() {
        return keyPair;
    }

    void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    byte[] getCipherTextDesCbc(byte[] bytes) throws Exception {
        return cbcCipher().doFinal(bytes);
    }

    private Cipher cbcCipher() throws Exception {
        if (cbcCipher == null) {
            cbcCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cbcCipher.init(Cipher.ENCRYPT_MODE, getDesKey());
        }
        return cbcCipher;
    }

    byte[] encodedParams() throws Exception {
        return cbcCipher().getParameters().getEncoded();
    }

    void phase1(byte[] counterPartyKey) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(counterPartyKey);
        PublicKey pubKey = getKeyFactory().generatePublic(x509KeySpec);
        System.out.println("Execute PHASE1 ...");
        getKeyAgree().doPhase(pubKey, true);
    }
}
