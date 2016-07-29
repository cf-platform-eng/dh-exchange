package io.pivotal.cf.dh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
class Server {

    @Autowired
    private Party bob;

    @Autowired
    private Util util;

    private HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @RequestMapping(value = "/server/pubKey", method = RequestMethod.POST)
    public ResponseEntity<String> pubKey(@RequestBody String pubKey) throws Exception {
        return new ResponseEntity<>(util.fromBytes(bob.getPublicKey(util.toBytes(pubKey))), httpHeaders(), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/server/secret", method = RequestMethod.POST)
    public ResponseEntity<String> secret(@RequestBody String pubKey) throws Exception {
        bob.sharedSecret(util.toBytes(pubKey));
        return new ResponseEntity<>(httpHeaders(), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/server/encryptDesEcb", method = RequestMethod.POST)
    public ResponseEntity<String> encryptDesEcb(@RequestBody String message) throws Exception {
        return new ResponseEntity<>(util.fromBytes(bob.getCipherTextDesEcb(util.toUtf8Bytes(message))), httpHeaders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/server/encryptDesCbc", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> encryptDesCbc(@RequestBody String message) throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("message", util.fromBytes(bob.getCipherTextDesCbc(util.toUtf8Bytes(message))));
        m.put("parameters", util.fromBytes(bob.encodedParams()));
        return new ResponseEntity<>(m, httpHeaders(), HttpStatus.OK);
    }
}