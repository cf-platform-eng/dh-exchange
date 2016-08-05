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
        String s = "bb8fde796aa7fd7180d6f9bd034922693ee8d8e245fa8426a6512b33316df090";
        byte[] b = util.toBytes(s);
        assertTrue(b.length > 0);
        String s1 = util.fromBytes(b);
        assertEquals(s, s1);
    }

    @Test
    public void testUTF8Codec() throws Exception {
        String s = "this Is A Test!";
        byte[] b = util.toUtf8Bytes(s);
        assertTrue(b.length > 0);
        String s1 = util.fromUtf8Bytes(b);
        assertEquals(s, s1);
    }
}