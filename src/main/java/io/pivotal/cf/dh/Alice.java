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

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * This program executes the Diffie-Hellman key agreement protocol
 * between 2 parties: Alice and Bob.
 */

@Service
class Alice extends Party {

    private void init() throws NoSuchAlgorithmException, InvalidKeyException {
        System.out.println("ALICE: Generate DH keypair ...");
        setKeyPair(getKeyPairGenerator().generateKeyPair());

        // Alice creates and initializes her DH KeyAgreement object
        System.out.println("ALICE: Initialization ...");
        setKeyAgree(KeyAgreement.getInstance("DH"));
        getKeyAgree().init(getKeyPair().getPrivate());
    }

    byte[] getPublicKeyEnc() throws Exception {
        if (getKeyPair() == null) {
            init();
        }
        return getKeyPair().getPublic().getEncoded();
    }

    void sharedSecret() throws Exception {
        setDesKey(getKeyAgree().generateSecret("DES"));
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
}