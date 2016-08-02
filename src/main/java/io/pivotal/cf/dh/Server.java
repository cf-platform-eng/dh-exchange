package io.pivotal.cf.dh;

import io.pivotal.cf.dh.service.ClientRepository;
import io.pivotal.cf.dh.service.QuoteRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
class Server {

    private static final Logger LOG = Logger.getLogger(Server.class);

    @Autowired
    private Party bob;

    @Autowired
    private Util util;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private HttpHeaders httpHeaders;

    @RequestMapping(value = "/server/pubKey", method = RequestMethod.GET)
    public ResponseEntity<String> pubKey(HttpRequest request) throws Exception {
        if(bob.hasSecrets()) {
            throw new Exception("keys already exchanged.");
        }

        //get requester from request, ask for their key
        //TODO hard coded
        //String uri = ...
        //String name = ...
        //store somewhere...
        String key = clientRepository.getKey();

        //create secret for them
        bob.sharedSecret(util.toBytes(key));

        //return my key
        return new ResponseEntity<>(util.fromBytes(bob.getPublicKey()), httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/server/secret", method = RequestMethod.POST)
    public ResponseEntity<String> secret(@RequestBody String pubKey) throws Exception {
        bob.sharedSecret(util.toBytes(pubKey));
        return new ResponseEntity<>(httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/server/encrypt", method = RequestMethod.POST)
    public ResponseEntity<String> encrypt(@RequestBody String message) throws Exception {
        return new ResponseEntity<>(bob.encrypt(message), httpHeaders, HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @RequestMapping(value = "/server/quote/{symbol}", method = RequestMethod.GET)
    public ResponseEntity<String> quote(@PathVariable String symbol, @RequestHeader HttpHeaders headers) throws Exception {
        //validate the request
        validate(headers, null);

        return new ResponseEntity<>(quoteRepository.getQuote("select * from yahoo.finance.quotes where symbol = '"
                + symbol + "'"), httpHeaders(headers), HttpStatus.OK);
    }

    HttpHeaders httpHeaders(HttpHeaders headers) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        headers.keySet();
//
//        List<String> l = new ArrayList<>();
//        l.add( "alice: fhfhfhfhhfh");
//        headers.put(HttpHeaders.AUTHORIZATION,l);
        return h;
    }

    private void validate(HttpHeaders headers, String content) throws GeneralSecurityException {
        String hmac = headers.get("Authorization").get(0);

//        if(hmac == null) {
//            throw new GeneralSecurityException("Authorization token missing from request.");
//        }

        String date = new Date(headers.getDate()).toString();
        String method = headers.get("Method").get(0);
        String uri = headers.getLocation().toString();
        String s = toSign(date, method, uri, content);

        //TODO, look secret up
        String computedHmac = bob.hmac(s);

        if( ! hmac.equals(computedHmac)) {
            throw new GeneralSecurityException("Invalid hmac token.");
        }
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
        LOG.info("server signing: " + ret);
        return ret;
    }
}