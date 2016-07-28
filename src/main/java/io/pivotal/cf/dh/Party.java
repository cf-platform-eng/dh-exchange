package io.pivotal.cf.dh;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

abstract class Party {

    private KeyPairGenerator keyPairGenerator;

    private KeyFactory keyFactory;

    private KeyAgreement keyAgree;

    private SecretKey desKey;

    private KeyPair keyPair;

    private Cipher cbcCipher;

    public Party(KeyPairGenerator keyPairGenerator, KeyFactory keyFactory) throws InvalidKeyException, NoSuchAlgorithmException {
        super();
        this.keyPairGenerator = keyPairGenerator;
        this.keyFactory = keyFactory;
        init();
    }

    KeyFactory getKeyFactory() {
        return keyFactory;
    }

    KeyAgreement getKeyAgree() throws NoSuchAlgorithmException {
        if(keyAgree == null) {
            setKeyAgree(KeyAgreement.getInstance("DH"));
        }
        return keyAgree;
    }

    private void setKeyAgree(KeyAgreement keyAgree) {
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

    void sharedSecret(byte[] counterPartyKey) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(counterPartyKey);
        PublicKey pubKey = getKeyFactory().generatePublic(x509KeySpec);
        System.out.println("Execute PHASE1 ...");
        getKeyAgree().doPhase(pubKey, true);
        setDesKey(getKeyAgree().generateSecret("DES"));
    }

    private void init() throws NoSuchAlgorithmException, InvalidKeyException {
        System.out.println("Generate DH keypair ...");
        setKeyPair(keyPairGenerator.generateKeyPair());

        // Alice creates and initializes her DH KeyAgreement object
        System.out.println("Initialization ...");
        getKeyAgree().init(getKeyPair().getPrivate());
    }

    byte[] getPublicKey() throws Exception {
        return getKeyPair().getPublic().getEncoded();
    }
}
