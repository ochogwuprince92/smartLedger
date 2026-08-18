package com.finance.smartLedger.security.filter;

import com.finance.smartLedger.security.domain.ServiceCredential;
import com.finance.smartLedger.security.infrastructure.persistence.ServiceCredentialRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ServiceApiKeyAuthenticationFilter extends OncePerRequestFilter {

  private static final String API_KEY_HEADER = "X-Service-Api-Key";

  private final ServiceCredentialRepository serviceCredentialRepository;
  private final PasswordEncoder passwordEncoder;

  public ServiceApiKeyAuthenticationFilter(
      ServiceCredentialRepository serviceCredentialRepository, @Lazy PasswordEncoder passwordEncoder) {
    this.serviceCredentialRepository = serviceCredentialRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String apiKey = request.getHeader(API_KEY_HEADER);

    // If no API key header, defer to JWT filter (do nothing)
    if (apiKey == null || apiKey.isBlank()) {
      filterChain.doFilter(request, response);
      return;
    }

    // Find credential by checking all enabled credentials
    var enabledCredentials = serviceCredentialRepository.findByEnabledTrue();
    ServiceCredential matchedCredential = null;

    for (ServiceCredential credential : enabledCredentials) {
      if (passwordEncoder.matches(apiKey, credential.getHashedApiKey())) {
        matchedCredential = credential;
        break;
      }
    }

    if (matchedCredential == null) {
      // Invalid API key - return 401
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("{\"error\":\"Invalid API key\"}");
      return;
    }

    // Check if credential is enabled (redundant check since we only query enabled ones, but safety net)
    if (!matchedCredential.isEnabled()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("{\"error\":\"Service credential is disabled\"}");
      return;
    }

    // Create authentication with granted permissions as authorities
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            matchedCredential.getName(),
            null,
            matchedCredential.getGrantedPermissions().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);
  }
}
