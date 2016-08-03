package io.pivotal.cf.dh;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@RestController
class Client {

    private static final Logger LOG = LogManager.getLogger(Client.class);

    @Autowired
    private Party alice;

    @Autowired
    private Util util;

    @Autowired
    private ServerRepository serverRepository;

    @RequestMapping(value = "/client/pubKey", method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> pubKey() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put(alice.getName(), util.fromBytes(alice.getPublicKey()));
        return new ResponseEntity<>(m, HttpStatus.OK);
    }

    @RequestMapping(value = "/client/exchange", method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> exchange() throws GeneralSecurityException {
        Map<String, String> m = serverRepository.getKey(alice.getName(), util.fromBytes(alice.getPublicKey()));
        alice.sharedSecret(util.toBytes(m.values().iterator().next()));
        return new ResponseEntity<>(m, HttpStatus.CREATED);
    }

//    @RequestMapping(value = "/client/decrypt", method = RequestMethod.POST)
//    public ResponseEntity<String> decrypt(@RequestBody String message) throws Exception {
//        return new ResponseEntity<>(alice.decrypt(message), HttpStatus.OK);
//    }

    @RequestMapping(value = "/client/quote/{symbol}", method = RequestMethod.GET)
    public ResponseEntity<String> quote(@PathVariable String symbol, HttpServletRequest request) throws Exception {
        if (!alice.hasSecrets()) {
            throw new Exception("key exchange required before making this call.");
        }

        Map<String, Object> m = headerMap(request, null);
        return new ResponseEntity<>(serverRepository.getQuote(symbol, m), HttpStatus.OK);
    }

    private Map<String, Object> headerMap(HttpServletRequest request, String content) throws Exception {
        Map<String, Object> m = new HashMap<>();
        m.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        String date = util.currentHttpTime();
        m.put(HttpHeaders.DATE, date);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String toSign = toSign(date, method, uri, content);

        m.put(HttpHeaders.AUTHORIZATION, alice.getName() + ":" + alice.hmac(toSign));
        return m;
    }

    private String toSign(String date, String method, String uri, String content) {
        StringBuilder sb = new StringBuilder();
        sb.append(date).append("\n")
                .append(method).append("\n")
                .append(uri);

        if (content != null) {
            sb.append("\n").append(content);
        }

        String ret = sb.toString();
        LOG.info("client signing: " + ret);
        return ret;
    }
}