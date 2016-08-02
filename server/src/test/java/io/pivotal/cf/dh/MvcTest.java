package io.pivotal.cf.dh;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import static junit.framework.TestCase.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
@WebAppConfiguration
@Ignore
public class MvcTest {

//    @Autowired
//    ClientRepository client;

//    @Autowired
//    Server server;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testDHexchange() throws Exception {
//        MvcResult result = mockMvc.perform(get("/client/pubKey"))
//                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
//
//        String clientKey = result.getResponse().getContentAsString();
//        assertNotNull(clientKey);

        MvcResult result = mockMvc.perform(get("/server/pubKey"))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        String serverKey = result.getResponse().getContentAsString();
        assertNotNull(serverKey);

//        mockMvc.perform(post("/client/secret").content(serverKey))
//                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
//
//        mockMvc.perform(post("/server/secret").content(clientKey))
//                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        String testmessage = "Hello World!";
        result = mockMvc.perform(post("/server/encrypt").content(testmessage))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        String s1 = result.getResponse().getContentAsString();
        assertNotNull(s1);

//        result = mockMvc.perform(post("/client/decrypt").content(s1))
//                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
//
//        String s2 = result.getResponse().getContentAsString();
//        assertNotNull(s2);
//        assertEquals(testmessage, s2);


        result = mockMvc.perform(get("/server/quote/GOOG"))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        String quote = result.getResponse().getContentAsString();
        assertNotNull(quote);

//        result = mockMvc.perform(get("/client/quote/GOOG"))
//                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
//
//        quote = result.getResponse().getContentAsString();
//        assertNotNull(quote);
    }
}