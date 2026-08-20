package com.geekychris.rpncalc.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsResultForValidExpression() throws Exception {
        mockMvc.perform(post("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\": \"3 4 +\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(7.0))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void returns400ForStackUnderflow() throws Exception {
        mockMvc.perform(post("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\": \"5 +\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    void returns400ForDivisionByZero() throws Exception {
        mockMvc.perform(post("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\": \"1 0 /\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Division by zero"));
    }

    @Test
    void returns400ForBlankExpression() throws Exception {
        mockMvc.perform(post("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void returns400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest());
    }
}
