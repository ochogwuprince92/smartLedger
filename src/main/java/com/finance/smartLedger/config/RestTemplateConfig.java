package com.finance.smartLedger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Value("${n8n.timeout:30000}")
  private int n8nTimeout;

  @Bean
  @Primary
  public RestTemplate restTemplate() {
    // Default RestTemplate with no timeout (for backward compatibility)
    // This is used by PaystackGatewayClient and other components
    return new RestTemplate();
  }

  @Bean("n8nRestTemplate")
  public RestTemplate n8nRestTemplate() {
    // Dedicated RestTemplate for n8n calls with configured timeout
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(n8nTimeout);
    factory.setReadTimeout(n8nTimeout);
    return new RestTemplate(factory);
  }
}
