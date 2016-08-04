package io.pivotal.cf.dh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
class Util {

    private static final Logger LOG = LogManager.getLogger(Util.class);

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

    Map<String, Object> headerMap(Party party, String method, String uri, String content) throws GeneralSecurityException {
        Map<String, Object> m = new HashMap<>();
        m.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8);

        String date = currentHttpTime();
        m.put(HttpHeaders.DATE, date);

        String toSign = signThis(date, method, uri, content);

        m.put(HttpHeaders.AUTHORIZATION, party.getName() + ":" + party.hmac(toSign));
        return m;
    }

    HttpHeaders responseHeaders(Party party, String method, String uri, String content) throws GeneralSecurityException {
        HttpHeaders h = new HttpHeaders();
        Map<String, Object> hm = headerMap(party, method, uri, content);
        for (String s : hm.keySet()) {
            h.set(s, hm.get(s).toString());
        }
        return h;
    }

    void validate(Party party, HttpServletRequest request, String content) throws GeneralSecurityException {
        String token = getToken(request);
        String date = request.getHeader("date");
        String method = request.getMethod();
        String uri = request.getRequestURI();

        validate(party, token, date, method, uri, content);
    }

    void validate(Party party, String token, String date, String method, String uri, String content) throws GeneralSecurityException {
        String signThis = signThis(date, method, uri, content);
        String computedHmac = party.hmac(signThis);

        if (!token.equals(computedHmac)) {
            throw new GeneralSecurityException("Invalid hmac token.");
        }
    }

    String getToken(HttpServletRequest request) throws GeneralSecurityException {
        return getAuth(request).split(":")[1];
    }

    String getName(HttpServletRequest request) throws GeneralSecurityException {
        return getAuth(request).split(":")[0];
    }

    private String getAuth(HttpServletRequest request) throws GeneralSecurityException {
        String auth = request.getHeader("Authorization");

        if (auth == null) {
            throw new GeneralSecurityException("Authorization token missing from request.");
        }
        return auth;
    }

    String toString(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    Map<String, Object> toMap(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Class c = new HashMap<String, Object>().getClass();
        return (Map<String, Object>) mapper.readValue(json, c);
    }

    String toJson(Object o) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(o);
    }
}