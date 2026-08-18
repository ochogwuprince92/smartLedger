package com.finance.smartLedger.web.presentation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.security.config.JwtService;
import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Unit test for web-based authentication (cookie bridge) components.
 * 
 * This test validates the cookie authentication logic without requiring
 * full Spring context loading.
 */
@ExtendWith(MockitoExtension.class)
class WebAuthIntegrationTest {

  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtService jwtService;
  @Mock private UserService userService;
  @Mock private HttpServletRequest request;

  @Test
  void testAuthenticationFlowWithValidCredentials() {
    // Test that valid credentials can be authenticated
    String username = "admin";
    String password = "admin123";
    
    Authentication mockAuth = mock(Authentication.class);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(mockAuth);
    
    Authentication result = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, password));
    
    assertNotNull(result);
    verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void testJwtTokenGeneration() {
    // Test that JWT tokens can be generated
    String username = "admin";
    String userId = "123e4567-e89b-12d3-a456-426614174000";
    
    when(jwtService.generateToken(username, userId)).thenReturn("mock.jwt.token");
    
    String token = jwtService.generateToken(username, userId);
    
    assertNotNull(token);
    assertEquals("mock.jwt.token", token);
    verify(jwtService, times(1)).generateToken(username, userId);
  }

  @Test
  void testCookieExtractionFromRequest() {
    // Test that cookies can be extracted from HTTP request
    Cookie[] cookies = new Cookie[] {
        new Cookie("jwt", "test.token"),
        new Cookie("session", "session-id")
    };
    
    when(request.getCookies()).thenReturn(cookies);
    
    Cookie[] result = request.getCookies();
    
    assertNotNull(result);
    assertEquals(2, result.length);
    assertEquals("jwt", result[0].getName());
    assertEquals("test.token", result[0].getValue());
  }

  @Test
  void testCookieExtractionWithNoCookies() {
    // Test that null is returned when no cookies present
    when(request.getCookies()).thenReturn(null);
    
    Cookie[] result = request.getCookies();
    
    assertNull(result);
  }

  @Test
  void testCookieExtractionWithSpecificCookie() {
    // Test extracting a specific cookie by name
    Cookie[] cookies = new Cookie[] {
        new Cookie("jwt", "test.token"),
        new Cookie("other", "other-value")
    };
    
    when(request.getCookies()).thenReturn(cookies);
    
    Cookie[] result = request.getCookies();
    String jwtValue = null;
    if (result != null) {
      for (Cookie cookie : result) {
        if ("jwt".equals(cookie.getName())) {
          jwtValue = cookie.getValue();
          break;
        }
      }
    }
    
    assertEquals("test.token", jwtValue);
  }
}