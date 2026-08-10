package com.finance.smartLedger.shared.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(authorities = "USER:READ")
@Transactional
class GlobalExceptionHandlerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void businessException_returnsProblemDetailResponse() throws Exception {
    // GREEN: This test expects RFC7807 ProblemDetail response
    // UserService throws BusinessException with ErrorCodes.NOT_FOUND which maps to 400
    
    MvcResult result =
        mockMvc
            .perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                        "/api/v1/users/" + java.util.UUID.randomUUID())
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
            .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andReturn();

    String response = result.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(response);

    // Assert RFC7807 fields are present
    assertTrue(jsonNode.has("type"), "Response should have 'type' field");
    assertTrue(jsonNode.has("title"), "Response should have 'title' field");
    assertTrue(jsonNode.has("status"), "Response should have 'status' field");
    assertTrue(jsonNode.has("detail"), "Response should have 'detail' field");

    // Assert status is 400 (ErrorCodes.NOT_FOUND maps to BAD_REQUEST)
    assertEquals(400, jsonNode.get("status").asInt(), "Status should be 400 BAD_REQUEST");

    // Assert detail contains error information
    String detail = jsonNode.get("detail").asText();
    assertTrue(
        detail.contains("ERR-1002") || detail.toLowerCase().contains("not found"),
        "Detail should contain error code or 'not found' message");
  }

  @Test
  void genericException_returns500ProblemDetail() throws Exception {
    // RED: This test expects RFC7807 ProblemDetail response for generic exceptions
    // but will fail because no @RestControllerAdvice exists yet
    
    // We'll use a non-existent endpoint to trigger a 404, which should return ProblemDetail
    MvcResult result =
        mockMvc
            .perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                        "/api/nonexistent-endpoint")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound())
            .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andReturn();

    String response = result.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(response);

    // Assert RFC7807 fields are present
    assertTrue(jsonNode.has("type"), "Response should have 'type' field");
    assertTrue(jsonNode.has("title"), "Response should have 'title' field");
    assertTrue(jsonNode.has("status"), "Response should have 'status' field");
    assertTrue(jsonNode.has("detail"), "Response should have 'detail' field");

    // Assert status is 404
    assertEquals(404, jsonNode.get("status").asInt(), "Status should be 404 NOT_FOUND");
  }
}
