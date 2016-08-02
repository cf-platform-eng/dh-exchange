package io.pivotal.cf.dh;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
class Util {

    //TODO make these configurable

    /**
     * https://www.rfc-editor.org/rfc/rfc7748.txt
     */
    static final String ELLIPTIC_KEY_TYPE = "EC";

    //    static final String DH_KEY_TYPE = "DH";
    static final String ELLIPTIC_KEY_AGREEMENT_TYPE = "ECDH";
    static final String DIGEST_TYPE = "SHA-256";
    static final String CIPHER_TYPE = "AES";
    static final int KEY_SIZE = 256;

    String fromUtf8Bytes(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, "utf-8");
    }

    byte[] toUtf8Bytes(String s) throws Exception {
        String base64encodedString = java.util.Base64.getEncoder().encodeToString(s.getBytes("utf-8"));
        return java.util.Base64.getDecoder().decode(base64encodedString);
    }

    String fromBytes(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes));
    }

    byte[] toBytes(String s) {
        return Base64.decodeBase64(s);
    }
}