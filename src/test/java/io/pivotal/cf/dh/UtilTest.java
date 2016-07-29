package io.pivotal.cf.dh;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static junit.framework.TestCase.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class UtilTest {

    @Autowired
    private Util util;

    @Autowired
    private Party alice;

    @Test
    public void testCodec() throws Exception {
        String s = "thisIsATest";
        byte[] b = util.toUtf8Bytes(s);
        assertTrue(b.length > 0);
        String s1 = util.fromUtf8Bytes(b);
        assertEquals(s, s1);
    }

    @Test
    public void testKeyCodec() throws Exception {
        byte[] key = alice.getPublicKey();
        String s = util.fromBytes(key);
        assertNotNull(s);
        byte[] key2 = util.toBytes(s);
        assertEquals(key.length, key2.length);
        for (int i = 0; i < key.length; i++) {
            assertEquals(key[i], key2[i]);
        }

        String s2 = util.fromBytes(key2);
        assertEquals(s, s2);
    }
}