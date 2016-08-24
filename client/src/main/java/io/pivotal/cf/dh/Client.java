package io.pivotal.cf.dh;

import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
class Client {

    @Autowired
    private Party alice;

    @Autowired
    private Util util;

    @Autowired
    private ServerRepository serverRepository;

    @RequestMapping(value = "/client/pubKey", method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> pubKey() throws GeneralSecurityException {
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
    public ResponseEntity<Object> quote(@PathVariable String symbol, HttpServletRequest request, HttpServletResponse response) throws GeneralSecurityException {
        if (!alice.hasSecrets()) {
            throw new GeneralSecurityException("key exchange required before making this call.");
        }

        //TODO way to calculate this?
        String uri = "/server/quote/" + symbol;
        String method = "GET";

        Map<String, Object> m = util.headerMap(alice, request.getMethod(), uri, null);

        Response result = serverRepository.getQuote(symbol, m);
        Map<String, Collection<String>> headers = result.headers();
        String content = util.toString(result.body());
        result.close();

        //validate the response
        String token = headers.get("Authorization").iterator().next().split(":")[1];
        String date = headers.get("Date").iterator().next();

        util.validate(alice, token, date, method, uri, content);

        return new ResponseEntity<>(util.toObject(content), HttpStatus.OK);
    }
}