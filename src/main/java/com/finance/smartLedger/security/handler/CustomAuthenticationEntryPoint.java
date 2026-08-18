package com.finance.smartLedger.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.smartLedger.shared.dto.ProblemDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Autowired
  public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

    // Check if this is a web page request (not API)
    String acceptHeader = request.getHeader("Accept");
    boolean isWebRequest = acceptHeader != null && acceptHeader.contains("text/html");

    if (isWebRequest) {
      // Redirect to login page for web requests
      response.sendRedirect("/login");
    } else {
      // Return JSON error for API requests
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      ProblemDetail problemDetail =
          ProblemDetail.of(
              HttpServletResponse.SC_UNAUTHORIZED,
              "Unauthorized",
              authException.getMessage(),
              "AUTH-001",
              request.getRequestURI(),
              LocalDateTime.now());

      objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }
  }
}
