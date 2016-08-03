package io.pivotal.cf.dh;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class TwoPartyTest {

    @Autowired
    private Party alice;

    @Autowired
    private Party bob;

    @Test
    public void testDHExchange() throws Exception {
        //alice = client
        //bob = server

        byte[] aliceKey = alice.getPublicKey();
        assertNotNull(aliceKey);

        byte[]  bobKey = bob.getPublicKey();
        assertNotNull(bobKey);

        //set up shared secrets
        alice.sharedSecret(bobKey);
        bob.sharedSecret(aliceKey);

        String s2 = "how now, brown cow";
        String crypto2 = bob.encrypt(s2);
        assertNotNull(crypto2);

        String decrypto2 = alice.decrypt(crypto2);
        assertNotNull(decrypto2);
        assertEquals(s2, decrypto2);
    }
}