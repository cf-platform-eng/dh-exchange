package io.pivotal.cf.dh;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@RestController
class Server {

    private static final Logger LOG = LogManager.getLogger(Server.class);

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
    public ResponseEntity<String> quote(@PathVariable String symbol, HttpServletRequest request) throws Exception {

        //validate the request
        validate(request, null);

        return new ResponseEntity<>(quoteRepository.getQuote("select * from yahoo.finance.quotes where symbol = '"
                + symbol + "'"), HttpStatus.OK);
    }

    private Party createParty(String name) {
        Party p = new Party(name);
        beanFactory.autowireBean(p);
        return p;
    }

    private void validate(HttpServletRequest request, String content) throws GeneralSecurityException {
        String auth = request.getHeader("Authorization");

        if (auth == null) {
            throw new GeneralSecurityException("Authorization token missing from request.");
        }

        String[] s = auth.split(":");
        Party p = secrets.get(s[0]);

        if (p == null) {
            throw new GeneralSecurityException("Party not found for name: " + s[0]);
        }

        String date = request.getHeader("date");
        String method = request.getMethod();
        String uri = request.getRequestURI();

        String c;
        if (content != null) {
            c = toSign(date, method, uri, content);
        } else {
            c = toSign(date, method, uri, "");
        }

        String computedHmac = p.hmac(c);

        if (!s[1].equals(computedHmac)) {
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