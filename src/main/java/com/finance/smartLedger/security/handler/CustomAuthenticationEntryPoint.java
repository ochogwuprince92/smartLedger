package com.finance.smartLedger.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.smartLedger.shared.dto.ProblemDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

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

    new ObjectMapper().writeValue(response.getOutputStream(), problemDetail);
  }
}
