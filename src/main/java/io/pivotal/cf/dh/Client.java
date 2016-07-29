package io.pivotal.cf.dh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
class Client {

    @Autowired
    private Party alice;

    @Autowired
    private Util util;

    private HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @RequestMapping(value = "/client/pubKey", method = RequestMethod.GET)
    public ResponseEntity<String> addChainAEntry() throws Exception {
        return new ResponseEntity<>(util.fromBytes(alice.getPublicKey()), httpHeaders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/client/secret", method = RequestMethod.POST)
    public ResponseEntity<String> getChainAEntry(@RequestBody String pubKey) throws Exception {
        alice.sharedSecret(util.toBytes(pubKey));
        return new ResponseEntity<>(httpHeaders(), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/client/decryptDesEcb", method = RequestMethod.POST)
    public ResponseEntity<String> decryptDesEcb(@RequestBody String message) throws Exception {
        return new ResponseEntity<>(util.fromUtf8Bytes(alice.decryptDesEcb(util.toBytes(message))), httpHeaders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/client/decryptDesCbc", method = RequestMethod.POST)
    public ResponseEntity<String> decryptDesCbc(@RequestBody Map<String, String> message) throws Exception {
        byte[] m = util.toBytes(message.get("message"));
        byte[] p = util.toBytes(message.get("parameters"));
        return new ResponseEntity<>(util.fromUtf8Bytes(alice.decryptDesCbc(m, p)), httpHeaders(), HttpStatus.OK);
    }
}