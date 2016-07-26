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
    private Alice alice;

    @Autowired
    private Bob bob;

    @Test
    public void testDHExchange() throws Exception {
//        byte[] aliceKey = alice.getPublicKeyEnc(true);
//        assertNotNull(aliceKey);

//        byte[] bobKey = bob.getPublicKeyEnc(true);
//        assertNotNull(bobKey);
//
//        alice.more();

        //init alice
        byte[] aliceKey = alice.getPublicKeyEnc(true);
        assertNotNull(aliceKey);

        //send key to bob and init bob with alice key
        byte[] bobKey = bob.getPublicKeyEnc(true, aliceKey);
        assertNotNull(bobKey);

        //phase 1
        alice.phase1(bobKey);
        bob.phase1(aliceKey);

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

        byte[] decrypto1 = alice.decryptDesCbc(crypto1);
        assertNotNull(decrypto1);
        assertEquals(s1, new String(decrypto1));
    }

}
