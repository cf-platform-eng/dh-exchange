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
class Client {

    @Autowired
    private Party alice;

    @Autowired
    private Util util;

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
}