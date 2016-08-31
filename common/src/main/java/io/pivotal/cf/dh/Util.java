package io.pivotal.cf.dh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response.Body;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
class Util {

    private static final Logger LOG = Logger.getLogger(Util.class);

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

    String fromUtf8Bytes(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    byte[] toUtf8Bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    String fromBytes(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    byte[] toBytes(String s) {
        return Base64.getDecoder().decode(s);
    }

    //TODO assumes "US"
    String currentHttpTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    String signThis(String date, String method, String uri, String content) {
        StringBuilder sb = new StringBuilder();
        sb.append(date).append(method).append(uri);

        if (content != null) {
            sb.append(content);
        }

        String ret = sb.toString();
        LOG.info("sign this: " + ret);
        return ret;
    }

    Map<String, Object> headerMap(Party signee, Party signer, String method, String uri, String content) throws GeneralSecurityException {
        Map<String, Object> m = new HashMap<>();
        m.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8);

        String date = currentHttpTime();
        m.put(HttpHeaders.DATE, date);

        String toSign = signThis(date, method, uri, content);

        m.put(HttpHeaders.AUTHORIZATION, signee.getName() + ":" + signer.hmac(toSign));
        return m;
    }

    HttpHeaders responseHeaders(Party signee, Party signer, String method, String uri, String content) throws GeneralSecurityException {
        HttpHeaders h = new HttpHeaders();
        Map<String, Object> hm = headerMap(signee, signer, method, uri, content);
        for (String s : hm.keySet()) {
            h.set(s, hm.get(s).toString());
        }
        return h;
    }

    void validate(Party party, String token, String date, String method, String uri, String content) throws GeneralSecurityException {
        validate(party, token, signThis(date, method, uri, content));
    }

    void validate(Party party, String token, String signature) throws GeneralSecurityException {
        String computedHmac = party.hmac(signature);

        if (!token.equals(computedHmac)) {
            throw new GeneralSecurityException("Invalid hmac token.");
        }
    }

    String getName(HttpServletRequest request) throws GeneralSecurityException {
        return getTokenPart(request, 0);
    }

    String getToken(HttpServletRequest request) throws GeneralSecurityException {
        return getTokenPart(request, 1);
    }

    private String getTokenPart(HttpServletRequest request, int index) throws GeneralSecurityException {
        String s = getAuth(request);
        if(s.contains(":")) {
            return s.split(":")[index];
        }
        return "";
    }

    private String getAuth(HttpServletRequest request) throws GeneralSecurityException {
        String auth = request.getHeader("Authorization");

        if (auth == null) {
            throw new GeneralSecurityException("Authorization token missing from request.");
        }
        return auth;
    }

    String toString(Body body) throws GeneralSecurityException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(body.asInputStream()))) {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
        return sb.toString();
    }

    Object toObject(String json) throws GeneralSecurityException {
        try {
            return new ObjectMapper().readValue(json, Object.class);
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
    }

    String toJson(Object o) throws GeneralSecurityException {
        try {
            return new ObjectMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new GeneralSecurityException(e);
        }
    }
}