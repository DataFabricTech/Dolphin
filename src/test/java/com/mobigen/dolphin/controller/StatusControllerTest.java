package com.mobigen.dolphin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobigen.dolphin.config.JobDBConfiguration;
import com.mobigen.dolphin.config.TrinoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@WebMvcTest(controllers = StatusController.class)
class StatusControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private JobDBConfiguration jobDBConfiguration;
    @MockBean
    private TrinoConfiguration trinoConfiguration;

    @Test
    public void testStatusOK() throws Exception {
        doReturn(true).when(jobDBConfiguration).isDBConnected();
        doReturn(true).when(trinoConfiguration).isDBConnected();
        mvc.perform(get("/api/status"))
                .andExpect(status().isOk());
    }

    @Test
    public void testStatusFail1() throws Exception {
        doReturn(false).when(jobDBConfiguration).isDBConnected();
        doReturn(true).when(trinoConfiguration).isDBConnected();
        var om = new ObjectMapper();
        var expected = om.writeValueAsString(Map.of("code", 500, "message", "Job DB connection is invalid"));
        mvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().json(expected));
    }

    @Test
    public void testStatusFail2() throws Exception {
        doReturn(true).when(jobDBConfiguration).isDBConnected();
        doReturn(false).when(trinoConfiguration).isDBConnected();
        var om = new ObjectMapper();
        var expected = om.writeValueAsString(Map.of("code", 500, "message", "trino connection is invalid"));
        mvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().json(expected));
    }
}