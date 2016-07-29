package io.pivotal.cf.dh;

import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

class Party {

    private static final Logger LOG = Logger.getLogger(Party.class);

    private KeyPairGenerator keyPairGenerator;

    private KeyFactory keyFactory;

    private KeyAgreement keyAgree;

    private SecretKey desKey;

    private KeyPair keyPair;

    private Cipher cbcCipher;

    Party(KeyPairGenerator keyPairGenerator, KeyFactory keyFactory) throws InvalidKeyException, NoSuchAlgorithmException {
        super();
        this.keyPairGenerator = keyPairGenerator;
        this.keyFactory = keyFactory;
        init();
    }

    private KeyFactory getKeyFactory() {
        return keyFactory;
    }

    private KeyAgreement getKeyAgree() throws NoSuchAlgorithmException {
        if (keyAgree == null) {
            setKeyAgree(KeyAgreement.getInstance("DH"));
        }
        return keyAgree;
    }

    private void setKeyAgree(KeyAgreement keyAgree) {
        this.keyAgree = keyAgree;
    }

    private SecretKey getDesKey() {
        return desKey;
    }

    private void setDesKey(SecretKey desKey) {
        this.desKey = desKey;
    }

    private KeyPair getKeyPair() {
        return keyPair;
    }

    private void setKeyPair(KeyPair keyPair) {
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
        LOG.info("Execute PHASE1.");
        getKeyAgree().doPhase(pubKey, true);

        LOG.info("Generate shared secret.");
        setDesKey(getKeyAgree().generateSecret("DES"));
    }

    private void init() throws NoSuchAlgorithmException, InvalidKeyException {
        LOG.info("Generate DH keypair.");
        setKeyPair(keyPairGenerator.generateKeyPair());
        LOG.info("Initialize DH keypair.");
        getKeyAgree().init(getKeyPair().getPrivate());
    }

    byte[] getPublicKey() throws Exception {
        return getKeyPair().getPublic().getEncoded();
    }

    byte[] getPublicKey(byte[] counterPartyKey) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec
                (counterPartyKey);
        Key counterPartyPubKey = getKeyFactory().generatePublic(x509KeySpec);
        DHParameterSpec dhParamSpec = ((DHPublicKey) counterPartyPubKey).getParams();
        LOG.info("Generate DH keypair ...");
        keyPairGenerator.initialize(dhParamSpec);
        setKeyPair(keyPairGenerator.generateKeyPair());

        LOG.info("Initialize DH keypair.");
        getKeyAgree().init(getKeyPair().getPrivate());

        return getKeyPair().getPublic().getEncoded();
    }

    byte[] decryptDesEcb(byte[] bytes) throws Exception {
        Cipher aliceCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        aliceCipher.init(Cipher.DECRYPT_MODE, getDesKey());
        return aliceCipher.doFinal(bytes);
    }

    byte[] decryptDesCbc(byte[] bytes, byte[] params) throws Exception {
        AlgorithmParameters ap = AlgorithmParameters.getInstance("DES");
        ap.init(params);
        Cipher aliceCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        aliceCipher.init(Cipher.DECRYPT_MODE, getDesKey(), ap);
        return aliceCipher.doFinal(bytes);
    }

    byte[] getCipherTextDesEcb(byte[] bytes) throws Exception {
        Cipher bobCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        bobCipher.init(Cipher.ENCRYPT_MODE, getDesKey());
        return bobCipher.doFinal(bytes);
    }
}
