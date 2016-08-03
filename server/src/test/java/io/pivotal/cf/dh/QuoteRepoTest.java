package io.pivotal.cf.dh;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Map;

import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class QuoteRepoTest {

    @Autowired
    private QuoteRepository repo;

    @Test
    public void testQuote() throws Exception {
        String query = "select * from yahoo.finance.quotes where symbol = 'GOOG'";
        Map<String, Object> m = repo.getQuote(query);
        assertNotNull(m);
    }
}