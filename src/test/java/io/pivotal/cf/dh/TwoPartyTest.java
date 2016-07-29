package io.pivotal.cf.dh;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;

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

        //send key to bob
        byte[] bobKey = bob.getPublicKey(aliceKey);
        assertNotNull(bobKey);

        //set up shared secrets
        alice.sharedSecret(bobKey);
        bob.sharedSecret(aliceKey);

        String s2 = "how now, brown cow";
        byte[] crypto2 = bob.getCipherTextDesEcb(s2.getBytes());
        assertNotNull(crypto2);

        byte[] decrypto2 = alice.decryptDesEcb(crypto2);
        assertNotNull(decrypto2);
        assertEquals(s2, new String(decrypto2));

        //start encrypt/decrypt tests
        String s1 = "testing 1, 2, 3...";
        byte[] crypto1 = bob.getCipherTextDesCbc(s1.getBytes());
        assertNotNull(crypto1);

        byte[] decrypto1 = alice.decryptDesCbc(crypto1, bob.encodedParams());
        assertNotNull(decrypto1);
        assertEquals(s1, new String(decrypto1));
    }
}