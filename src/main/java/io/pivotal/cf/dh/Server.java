package io.pivotal.cf.dh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
class Server {

    @Autowired
    private Party bob;

    @Autowired
    private Util util;

    @Autowired
    private HttpHeaders httpHeaders;

    @RequestMapping(value = "/server/pubKey", method = RequestMethod.GET)
    public ResponseEntity<String> pubKey() throws Exception {
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
}