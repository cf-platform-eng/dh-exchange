package io.pivotal.cf.dh;

/*
 * Copyright (c) 1997, 2001, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

/**
 * This program executes the Diffie-Hellman key agreement protocol
 * between 2 parties: Alice and Bob.
 * <p>
 * By default, preconfigured parameters (1024-bit prime modulus and base
 * generator used by SKIP) are used.
 * If this program is called with the "-gen" option, a new set of
 * parameters is created.
 */

@Service
class DHKeyAgreement2Oracle {

    @Autowired
    private Util util;

    void run(boolean generateDHParams) throws Exception {

        DHParameterSpec dhSkipParamSpec;

        if (generateDHParams) {
            // Some central authority creates new DH parameters
            System.out.println
                    ("Creating Diffie-Hellman parameters (takes VERY long) ...");
            AlgorithmParameterGenerator paramGen
                    = AlgorithmParameterGenerator.getInstance("DH");
            paramGen.init(512);
            AlgorithmParameters params = paramGen.generateParameters();
            dhSkipParamSpec = params.getParameterSpec
                    (DHParameterSpec.class);
        } else {
            // use some pre-generated, default DH parameters
            System.out.println("Using SKIP Diffie-Hellman parameters");
            dhSkipParamSpec = new DHParameterSpec(util.skip1024Modulus(),
                    util.skip1024Base());
        }

        /*
         * Alice creates her own DH key pair, using the DH parameters from
         * above
         */
        System.out.println("ALICE: Generate DH keypair ...");
        KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
        aliceKpairGen.initialize(dhSkipParamSpec);
        KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

        // Alice creates and initializes her DH KeyAgreement object
        System.out.println("ALICE: Initialization ...");
        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
        aliceKeyAgree.init(aliceKpair.getPrivate());

        // Alice encodes her public key, and sends it over to Bob.
        byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();

        /*
         * Let's turn over to Bob. Bob has received Alice's public key
         * in encoded format.
         * He instantiates a DH public key from the encoded key material.
         */
        KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec
                (alicePubKeyEnc);
        PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);

        /*
         * Bob gets the DH parameters associated with Alice's public key.
         * He must use the same parameters when he generates his own key
         * pair.
         */
        DHParameterSpec dhParamSpec = ((DHPublicKey) alicePubKey).getParams();

        // Bob creates his own DH key pair
        System.out.println("BOB: Generate DH keypair ...");
        KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
        bobKpairGen.initialize(dhParamSpec);
        KeyPair bobKpair = bobKpairGen.generateKeyPair();

        // Bob creates and initializes his DH KeyAgreement object
        System.out.println("BOB: Initialization ...");
        KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
        bobKeyAgree.init(bobKpair.getPrivate());

        // Bob encodes his public key, and sends it over to Alice.
        byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();

        /*
         * Alice uses Bob's public key for the first (and only) phase
         * of her version of the DH
         * protocol.
         * Before she can do so, she has to instantiate a DH public key
         * from Bob's encoded key material.
         */
        KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
        x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
        PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
        System.out.println("ALICE: Execute PHASE1 ...");
        aliceKeyAgree.doPhase(bobPubKey, true);

        /*
         * Bob uses Alice's public key for the first (and only) phase
         * of his version of the DH
         * protocol.
         */
        System.out.println("BOB: Execute PHASE1 ...");
        bobKeyAgree.doPhase(alicePubKey, true);

        /*
         * At this stage, both Alice and Bob have completed the DH key
         * agreement protocol.
         * Both generate the (same) shared secret.
         */
        byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
        int aliceLen = aliceSharedSecret.length;

        byte[] bobSharedSecret = new byte[aliceLen];
//        int bobLen;
//        try {
//            // show example of what happens if you
//            // provide an output buffer that is too short
//            bobLen = bobKeyAgree.generateSecret(bobSharedSecret, 1);
//        } catch (ShortBufferException e) {
//            System.out.println(e.getMessage());
//        }
        // provide output buffer of required size
//        bobLen =

                bobKeyAgree.generateSecret(bobSharedSecret, 0);

        System.out.println("Alice secret: " +
                util.toHexString(aliceSharedSecret));
        System.out.println("Bob secret: " +
                util.toHexString(bobSharedSecret));

        if (!java.util.Arrays.equals(aliceSharedSecret, bobSharedSecret))
            throw new Exception("Shared secrets differ");
        System.out.println("Shared secrets are the same");

        /*
         * Now let's return the shared secret as a SecretKey object
         * and use it for encryption. First, we generate SecretKeys for the
         * "DES" algorithm (based on the raw shared secret data) and
         * then we use DES in ECB mode
         * as the encryption algorithm. DES in ECB mode does not require any
         * parameters.
         *
         * Then we use DES in CBC mode, which requires an initialization
         * vector (IV) parameter. In CBC mode, you need to initialize the
         * Cipher object with an IV, which can be supplied using the
         * javax.crypto.spec.IvParameterSpec class. Note that you have to use
         * the same IV for encryption and decryption: If you use a different
         * IV for decryption than you used for encryption, decryption will
         * fail.
         *
         * NOTE: If you do not specify an IV when you initialize the
         * Cipher object for encryption, the underlying implementation
         * will generate a random one, which you have to retrieve using the
         * javax.crypto.Cipher.getParameters() method, which returns an
         * instance of java.security.AlgorithmParameters. You need to transfer
         * the contents of that object (e.g., in encoded format, obtained via
         * the AlgorithmParameters.getEncoded() method) to the party who will
         * do the decryption. When initializing the Cipher for decryption,
         * the (reinstantiated) AlgorithmParameters object must be passed to
         * the Cipher.init() method.
         */
        System.out.println("Return shared secret as SecretKey object ...");
        // Bob
        // NOTE: The call to bobKeyAgree.generateSecret above reset the key
        // agreement object, so we call doPhase again prior to another
        // generateSecret call
        bobKeyAgree.doPhase(alicePubKey, true);
        SecretKey bobDesKey = bobKeyAgree.generateSecret("DES");

        // Alice
        // NOTE: The call to aliceKeyAgree.generateSecret above reset the key
        // agreement object, so we call doPhase again prior to another
        // generateSecret call
        aliceKeyAgree.doPhase(bobPubKey, true);
        SecretKey aliceDesKey = aliceKeyAgree.generateSecret("DES");

        /*
         * Bob encrypts, using DES in ECB mode
         */
        Cipher bobCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        bobCipher.init(Cipher.ENCRYPT_MODE, bobDesKey);

        byte[] cleartext = "This is just an example".getBytes();
        byte[] ciphertext = bobCipher.doFinal(cleartext);

        /*
         * Alice decrypts, using DES in ECB mode
         */
        Cipher aliceCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        aliceCipher.init(Cipher.DECRYPT_MODE, aliceDesKey);
        byte[] recovered = aliceCipher.doFinal(ciphertext);

        if (!java.util.Arrays.equals(cleartext, recovered))
            throw new Exception("DES in CBC mode recovered text is " +
                    "different from cleartext");
        System.out.println("DES in ECB mode recovered text is " +
                "same as cleartext");

        /*
         * Bob encrypts, using DES in CBC mode
         */
        bobCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        bobCipher.init(Cipher.ENCRYPT_MODE, bobDesKey);

        cleartext = "This is just an example".getBytes();
        ciphertext = bobCipher.doFinal(cleartext);
        // Retrieve the parameter that was used, and transfer it to Alice in
        // encoded format
        byte[] encodedParams = bobCipher.getParameters().getEncoded();

        /*
         * Alice decrypts, using DES in CBC mode
         */
        // Instantiate AlgorithmParameters object from parameter encoding
        // obtained from Bob
        AlgorithmParameters params = AlgorithmParameters.getInstance("DES");
        params.init(encodedParams);
        aliceCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        aliceCipher.init(Cipher.DECRYPT_MODE, aliceDesKey, params);
        recovered = aliceCipher.doFinal(ciphertext);

        if (!java.util.Arrays.equals(cleartext, recovered))
            throw new Exception("DES in CBC mode recovered text is " +
                    "different from cleartext");
        System.out.println("DES in CBC mode recovered text is " +
                "same as cleartext");
    }
}