package io.pivotal.cf.dh;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class UtilTest {

    @Autowired
    private Util util = new Util();

    @Test
    public void testCodec() throws Exception {
        String s = "thisIsATest";
        byte[] b = util.toUtf8Bytes(s);
        assertTrue(b.length > 0);
        String s1 = util.fromUtf8Bytes(b);
        assertEquals(s, s1);
    }
}