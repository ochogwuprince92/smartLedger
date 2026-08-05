package com.finance.smartLedger.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.smartLedger.shared.dto.ProblemDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ProblemDetail problemDetail =
        ProblemDetail.of(
            HttpServletResponse.SC_FORBIDDEN,
            "Forbidden",
            accessDeniedException.getMessage(),
            "AUTH-002",
            request.getRequestURI(),
            LocalDateTime.now());

    new ObjectMapper().writeValue(response.getOutputStream(), problemDetail);
  }
}
