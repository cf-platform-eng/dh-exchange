package io.pivotal.cf.dh;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class UtilTest {

    @Autowired
    private Util util;

    @Autowired
    private Party tester;

    @Test
    public void testCodec() throws Exception {
        String s = "thisIsATest";
        byte[] b = util.toUtf8Bytes(s);
        assertTrue(b.length > 0);
        String s1 = util.fromUtf8Bytes(b);
        assertEquals(s, s1);
    }
}