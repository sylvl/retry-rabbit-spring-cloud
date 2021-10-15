package org.sylvl.retryrabbit;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan
@ConfigurationPropertiesScan
@Configuration
public class RetryRabbitConfig {
  @Bean
  public RetryRouter retryRouter(RetryProperties retryProperties, StreamBridge streamBridge) {
    return new RetryRouter(retryProperties, streamBridge);
  }
}
