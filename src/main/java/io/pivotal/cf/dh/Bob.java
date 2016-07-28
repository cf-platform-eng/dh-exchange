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

import javax.crypto.Cipher;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

/**
 * This program executes the Diffie-Hellman key agreement protocol
 * between 2 parties: Alice and Bob.
 */

class Bob extends Party {

    Bob(KeyPairGenerator keyPairGenerator, KeyFactory keyFactory) throws InvalidKeyException, NoSuchAlgorithmException {
        super(keyPairGenerator, keyFactory);
    }

    byte[] getPublicKey(byte[] aliceKey) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec
                (aliceKey);
        Key alicePubKey = getKeyFactory().generatePublic(x509KeySpec);

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
        setKeyPair(bobKpairGen.generateKeyPair());

        // Bob creates and initializes his DH KeyAgreement object
        System.out.println("BOB: Initialization ...");
        getKeyAgree().init(getKeyPair().getPrivate());

        // Bob encodes his public key, and sends it over to Alice.
        return getKeyPair().getPublic().getEncoded();
    }

    byte[] getCipherTextDesEcb(byte[] bytes) throws Exception {
        Cipher bobCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        bobCipher.init(Cipher.ENCRYPT_MODE, getDesKey());
        return bobCipher.doFinal(bytes);
    }
}