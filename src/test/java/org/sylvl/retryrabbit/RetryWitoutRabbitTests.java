package org.sylvl.retryrabbit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestPropertySource("classpath:test-delay.properties")
@SpringBootTest
class RetryWitoutRabbitTests {

  @Autowired
  private RetryRouter retryRouter;

  @Autowired
  private RetryProperties retryProperties;

  @Autowired
  private OutputDestination output;

  @Test
  void shouldIncreaseRetryCountAndEndWithParking() {
    final Message<String> retryMessage1 = retryRouter.delayMessage(MessageBuilder.withPayload("test payload").build()).build();
    assertEquals(1, retryMessage1.getHeaders().get(retryProperties.getRetryCountHeader()));
    assertEquals(retryProperties.getInputExchangeName(), retryMessage1.getHeaders().get(retryProperties.getRetryDestinationHeader()));
    final Message<String> retryMessage2 = retryRouter.delayMessage(retryMessage1).build();
    assertEquals(2, retryMessage2.getHeaders().get(retryProperties.getRetryCountHeader()));
    assertEquals(retryProperties.getInputExchangeName(), retryMessage2.getHeaders().get(retryProperties.getRetryDestinationHeader()));
    final Message<String> retryMessage3 = retryRouter.delayMessage(retryMessage2).build();
    assertEquals("parking", retryMessage3.getHeaders().get("spring.cloud.stream.sendto.destination"));
  }

  @Test
  void shouldSendMessageToRetryQueueAndEndWithParking() {
    retryRouter.sendRetry(MessageBuilder.withPayload("test").build());
    final Message<byte[]> retryMessage1 = output.receive(1000, "delay1");
    assertEquals(1, retryMessage1.getHeaders().get(retryProperties.getRetryCountHeader()));
    assertEquals(retryProperties.getInputExchangeName(), retryMessage1.getHeaders().get(retryProperties.getRetryDestinationHeader()));
    retryRouter.sendRetry(retryMessage1);
    final Message<byte[]> retryMessage2 = output.receive(1000, "delay2");
    assertEquals(2, retryMessage2.getHeaders().get(retryProperties.getRetryCountHeader()));
    assertEquals(retryProperties.getInputExchangeName(), retryMessage2.getHeaders().get(retryProperties.getRetryDestinationHeader()));
    retryRouter.sendRetry(retryMessage2);
    final Message<byte[]> parkingMessage = output.receive(1000, "parking");
    assertNotNull(parkingMessage);
  }

  @Import(TestChannelBinderConfiguration.class)
  @SpringBootApplication(scanBasePackageClasses = RetryRabbitConfig.class)
  static class TestApp {
  }

}
