package io.pivotal.cf.dh;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
class Client {

    private static final Logger LOG = LogManager.getLogger(Client.class);

    @Autowired
    private Party alice;

    @Autowired
    private Util util;

    @Autowired
    private ServerRepository serverRepository;

    //TODO use feign instead, plus eureka maybe?
    @Autowired
    private Party bob;

    @Autowired
    private HttpHeaders httpHeaders;

    @RequestMapping(value = "/client/pubKey", method = RequestMethod.GET)
    public ResponseEntity<String> pubKey() throws Exception {
        return new ResponseEntity<>(util.fromBytes(alice.getPublicKey()), httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/client/secret", method = RequestMethod.POST)
    public ResponseEntity<String> secret(@RequestBody String pubKey) throws Exception {
        alice.sharedSecret(util.toBytes(pubKey));
        return new ResponseEntity<>(httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/client/decrypt", method = RequestMethod.POST)
    public ResponseEntity<String> decrypt(@RequestBody String message) throws Exception {
        return new ResponseEntity<>(alice.decrypt(message), httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/client/quote/{symbol}", method = RequestMethod.GET)
    public ResponseEntity<String> quote(@PathVariable String symbol, HttpServletRequest request) throws Exception {
        if(! alice.hasSecrets()) {
            throw new Exception("key exchange required before making this call.");
        }
        HttpHeaders h = httpHeaders(request, null);
        return new ResponseEntity<>(serverRepository.getQuote(symbol), h, HttpStatus.OK);
    }

    @RequestMapping(value = "/client/exchange", method = RequestMethod.GET)
    public void exhangeKeys() throws GeneralSecurityException {
        if(alice.hasSecrets()) {
            throw new GeneralSecurityException("keys already exchanged.");
        }

        //ask server for it's key
        byte[] key = bob.getPublicKey();

        //create secrets
        alice.sharedSecret(key);
    }

    private HttpHeaders httpHeaders(HttpServletRequest request, String content) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Date d = new Date();
        headers.setDate(d.getTime());

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String toSign = toSign(d, method, uri, content);

        List<String> l = new ArrayList<>();
        l.add(alice.getName() + ":" + alice.hmac(toSign));
        headers.put(HttpHeaders.AUTHORIZATION, l);
        return headers;
    }

    private String toSign(Date date, String method, String uri, String content) {
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