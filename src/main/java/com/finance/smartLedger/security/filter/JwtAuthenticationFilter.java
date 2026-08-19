package com.finance.smartLedger.security.filter;

import com.finance.smartLedger.security.config.JwtService;
import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.infrastructure.persistence.UserRepository;
import com.finance.smartLedger.security.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService userDetailsService;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  public JwtAuthenticationFilter(
      JwtService jwtService,
      CustomUserDetailsService userDetailsService,
      UserRepository userRepository,
      ObjectMapper objectMapper) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.userRepository = userRepository;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String username;

    // First, try to get JWT from Authorization header
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      jwt = authHeader.substring(7);
      username = jwtService.extractUsername(jwt);
    } else {
      // Fallback: try to get JWT from cookie
      String jwtFromCookie = extractJwtFromCookie(request);
      if (jwtFromCookie != null) {
        jwt = jwtFromCookie;
        username = jwtService.extractUsername(jwt);
      } else {
        // No JWT found in header or cookie, proceed without authentication
        filterChain.doFilter(request, response);
        return;
      }
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        User user =
            userRepository
                .findByUsername(username)
                .orElse(null);

        if (user != null
            && Boolean.TRUE.equals(user.getMustChangePassword())
            && !isPasswordChangeEndpoint(request)) {
          sendPasswordChangeRequiredResponse(response);
          return;
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private String extractJwtFromCookie(HttpServletRequest request) {
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("jwt".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  private boolean isPasswordChangeEndpoint(HttpServletRequest request) {
    String path = request.getRequestURI();
    String method = request.getMethod();
    // Allow both JSON API endpoint and web page for password change
    return (path.matches("/api/v1/users/[a-f0-9-]+/password") && "PATCH".equalsIgnoreCase(method))
        || (path.equals("/api/v1/auth/change-password") && "POST".equalsIgnoreCase(method))
        || (path.equals("/change-password") && "GET".equalsIgnoreCase(method))
        || (path.equals("/change-password") && "POST".equalsIgnoreCase(method));
  }

  private void sendPasswordChangeRequiredResponse(HttpServletResponse response) throws IOException {
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType("application/json");

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "Password change required. Your account has been reset with a temporary password. You must change your password before accessing other resources.");
    problemDetail.setTitle("Password Change Required");
    problemDetail.setType(
        URI.create("https://api.smartledger.com/errors/PASSWORD_CHANGE_REQUIRED"));

    response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
  }
}
