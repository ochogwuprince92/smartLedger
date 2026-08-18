package com.finance.smartLedger.test.configuration;

import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@ComponentScan(
    basePackages = "com.finance.smartLedger",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.CUSTOM,
            classes = {AutoConfigurationExcludeFilter.class}
        )
    }
)
public class IntegrationTestConfiguration {
    
    // This configuration is used to disable problematic auto-configurations
    // for integration tests without modifying the main application
}
