package io.pivotal.cf.dh;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@RestController
class Server {

    private static final Logger LOG = Logger.getLogger(Server.class);

    private final Map<String, Party> secrets = new HashMap<>();

    @Autowired
    private Util util;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    @RequestMapping(value = "/server/pubKey", method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> pubKey(@RequestParam(value = "name") String name, @RequestParam(value = "pubKey") String pubKey) throws GeneralSecurityException {

        if (secrets.containsKey(name)) {
            throw new GeneralSecurityException("keys already exchanged with " + name);
        }

        Party p = createParty(name);
        String pPubKey = util.fromBytes(p.getPublicKey());
        p.sharedSecret(util.toBytes(pubKey));
        secrets.put(name, p);

        Map<String, String> m = new HashMap<>();
        m.put("server", pPubKey);

        return new ResponseEntity<>(m, HttpStatus.CREATED);
    }

//    @RequestMapping(value = "/server/encrypt", method = RequestMethod.POST)
//    public ResponseEntity<String> encrypt(@RequestBody String message) throws Exception {
//        return new ResponseEntity<>(bob.encrypt(message), HttpStatus.OK);
//    }

    @RequestMapping(value = "/server/quote/{symbol}", method = RequestMethod.GET)
    public ResponseEntity<String> quote(@PathVariable String symbol, HttpServletRequest request) throws GeneralSecurityException {

        //get the content
        String content = util.toJson(quoteRepository.getQuote("select * from yahoo.finance.quotes where symbol = '" + symbol + "'"));

        //prep and sign the response
        HttpHeaders h = util.responseHeaders(getParty(util.getName(request)), request.getMethod(), request.getRequestURI(), content);

        return new ResponseEntity<>(content, h, HttpStatus.OK);
    }

    private Party createParty(String name) {
        Party p = new Party(name);
        beanFactory.autowireBean(p);
        return p;
    }

    Party getParty(String name) {
        return secrets.get(name);
    }
}