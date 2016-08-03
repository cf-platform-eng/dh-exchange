package io.pivotal.cf.dh;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
@WebAppConfiguration
public class MvcTest {

    @Autowired
    private Party alice;

    @Autowired
    private Util util;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testDHexchange() throws Exception {
        String aliceKey = util.fromBytes(alice.getPublicKey());
        assertNotNull(aliceKey);

        MvcResult result = mockMvc.perform(get("/server/pubKey?name=alice&pubKey=" + aliceKey))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andReturn();

        Map<String, Object> serverResp = toMap(result.getResponse().getContentAsString());
        assertNotNull(serverResp);
        String serverKey = serverResp.get("server").toString();
        assertNotNull(serverKey);

        alice.sharedSecret(util.toBytes(serverKey));

//        String testmessage = "Hello World!";
//        result = mockMvc.perform(post("/server/encrypt").content(testmessage))
//                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
//
//        String s1 = result.getResponse().getContentAsString();
//        assertNotNull(s1);

//        result = mockMvc.perform(post("/client/decrypt").content(s1))
//                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
//
//        String s2 = result.getResponse().getContentAsString();
//        assertNotNull(s2);
//        assertEquals(testmessage, s2);

        String date = util.currentHttpTime();
        String signThis = util.signThis(date, "GET", "/server/quote/GOOG", null);
        String hmac = alice.hmac(signThis);

        result = mockMvc.perform(get("/server/quote/GOOG")
                .header("date", date)
                .header(HttpHeaders.AUTHORIZATION, alice.getName() + ":" + hmac))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andReturn();

        Map<String, Object> quote = toMap(result.getResponse().getContentAsString());
        assertNotNull(quote);
        assertNotNull(quote.get("query"));
    }

    private Map<String, Object> toMap(String s) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(s, new HashMap<String, Object>().getClass());
    }
}