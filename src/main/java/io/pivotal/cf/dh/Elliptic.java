package io.pivotal.cf.dh;

import javax.crypto.KeyAgreement;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

/**
 * from here: https://neilmadden.wordpress.com/2016/05/20/ephemeral-elliptic-curve-diffie-hellman-key-agreement-in-java/
 */
public class Elliptic {

    public static void main(String[] args) throws Exception {
        // Generate ephemeral ECDH keypair
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        KeyPair kp = kpg.generateKeyPair();
        byte[] ourPk = kp.getPublic().getEncoded();

        // Display our public key
        System.out.println("Our public Key:   " + printHexBinary(ourPk));

        // Read other's public key:
        KeyPair otherKp = kpg.generateKeyPair();
        byte[] otherPk = otherKp.getPublic().getEncoded();
        System.out.println("Other public Key: " + printHexBinary(otherPk));

        KeyFactory kf = KeyFactory.getInstance("EC");
        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(otherPk);
        PublicKey otherPublicKey = kf.generatePublic(pkSpec);

        // Perform key agreement
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(kp.getPrivate());
        ka.doPhase(otherPublicKey, true);

        // Read shared secret
        byte[] sharedSecret = ka.generateSecret();
        System.out.println("Shared secret: " + printHexBinary(sharedSecret));

        // Derive a key from the shared secret and both public keys
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        hash.update(sharedSecret);
        // Simple deterministic ordering
        List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(ourPk), ByteBuffer.wrap(otherPk));
        Collections.sort(keys);
        hash.update(keys.get(0));
        hash.update(keys.get(1));

        byte[] derivedKey = hash.digest();
        System.out.println("Final key: " + printHexBinary(derivedKey));
    }
}