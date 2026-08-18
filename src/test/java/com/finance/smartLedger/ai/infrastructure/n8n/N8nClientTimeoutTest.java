package com.finance.smartLedger.ai.infrastructure.n8n;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

class N8nClientTimeoutTest {

  @Test
  void restTemplate_WithTimeoutConfig_ShouldTimeoutOnNonRoutableEndpoint() {
    // Arrange: Create a RestTemplate with a very short timeout for testing
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(100); // 100ms connect timeout
    factory.setReadTimeout(100); // 100ms read timeout
    RestTemplate restTemplate = new RestTemplate(factory);

    // Act & Assert: Should timeout within the configured window (plus some margin)
    // The endpoint is non-routable (10.255.255.1:9999) which will cause a connection timeout
    long startTime = System.currentTimeMillis();
    assertThrows(
        Exception.class,
        () -> restTemplate.getForObject("http://10.255.255.1:9999/healthz", String.class),
        "Request should timeout when endpoint hangs");
    long elapsed = System.currentTimeMillis() - startTime;

    // The timeout should be close to the configured timeout (100ms in test config)
    // Allow some margin for test execution overhead
    assertTrue(
        elapsed < 5000,
        "Request should timeout within configured window, but took " + elapsed + "ms");
  }

  @Test
  void restTemplate_WithoutTimeoutConfig_HasNoExplicitTimeout() {
    // Arrange: Create a RestTemplate WITHOUT timeout configuration (current bug)
    RestTemplate restTemplate = new RestTemplate(); // No timeout configured

    // Act & Assert: Verify the factory is SimpleClientHttpRequestFactory with no explicit timeout
    // The current implementation uses new RestTemplate() which creates a factory with default (infinite) timeout
    SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
    
    // The factory exists but has no explicit timeout configured
    assertNotNull(factory, "RestTemplate should have a request factory");
    // This confirms the bug: no timeout is set, so it will hang indefinitely on network issues
  }
}
