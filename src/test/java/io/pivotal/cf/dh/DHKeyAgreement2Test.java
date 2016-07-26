package io.pivotal.cf.dh;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class DHKeyAgreement2Test {

    @Autowired
    private DHKeyAgreement2Oracle dh;

    @Test
    public void testDHSkip() throws Exception {
        dh.run(false);
    }

    @Test
    public void testDHGenerate() throws Exception {
        dh.run(true);
    }
}
