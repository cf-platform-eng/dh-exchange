package io.pivotal.cf.dh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
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

import java.io.IOException;
import java.util.HashMap;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
@WebAppConfiguration
public class MvcTest {

    @Autowired
    Client client;

    @Autowired
    Server server;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testDHexchange() throws Exception {
        MvcResult result = mockMvc.perform(get("/client/pubKey"))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        String clientKey = result.getResponse().getContentAsString();
        assertNotNull(clientKey);

        result = mockMvc.perform(post("/server/pubKey").content(clientKey))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        String serverKey = result.getResponse().getContentAsString();
        assertNotNull(serverKey);

        mockMvc.perform(post("/client/secret").content(serverKey))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        mockMvc.perform(post("/server/secret").content(clientKey))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        String testmessage = "Hello World!";
        result = mockMvc.perform(post("/server/encryptDesEcb").content(testmessage))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        String s1 = result.getResponse().getContentAsString();
        assertNotNull(s1);

        result = mockMvc.perform(post("/client/decryptDesEcb").content(s1))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        String s2 = result.getResponse().getContentAsString();
        assertNotNull(s2);
        assertEquals(testmessage, s2);

        result = mockMvc.perform(post("/server/encryptDesCbc").content(testmessage))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        String s3 = result.getResponse().getContentAsString();
        assertNotNull(s3);

        HashMap<String, String> m = (HashMap<String, String>) toObject(s3, new HashMap<String, String>().getClass());
        assertNotNull(m);

        result = mockMvc.perform(post("/client/decryptDesCbc").contentType(MediaType.APPLICATION_JSON).content(toJson(m)))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        String s4 = result.getResponse().getContentAsString();
        assertNotNull(s4);
        assertEquals(testmessage, s4);
    }

    private Object toObject(String json, Class clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, clazz);
    }

    private String toJson(Object o) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(o);
    }
}