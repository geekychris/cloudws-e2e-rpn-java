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
class SymbolicMathControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void simplifiesExpressionWithVariables() throws Exception {
        mockMvc.perform(post("/symbolic/simplify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\": \"x 0 +\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("x"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void differentiatesExpressionWithRespectToVariable() throws Exception {
        mockMvc.perform(post("/symbolic/differentiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\": \"x x *\", \"variable\": \"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("2 * x"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void returns400ForBlankSimplifyExpression() throws Exception {
        mockMvc.perform(post("/symbolic/simplify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void returns400ForMissingDifferentiateVariable() throws Exception {
        mockMvc.perform(post("/symbolic/differentiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\": \"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void returns400ForUnsupportedDifferentiation() throws Exception {
        mockMvc.perform(post("/symbolic/differentiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\": \"x y ^\", \"variable\": \"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
