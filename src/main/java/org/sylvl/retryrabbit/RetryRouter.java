package org.sylvl.retryrabbit;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class RetryRouter {

  private static final String SPRING_CLOUD_STREAM_SENDTO_HEADER = "spring.cloud.stream.sendto.destination";
  private static final String DELAY_CHANNEL_PREFIX = "delay";
  private final RetryProperties retryProperties;

  private final StreamBridge streamBridge;

  private final int maxRetry;

  public RetryRouter(RetryProperties retryProperties, StreamBridge streamBridge) {
    this.retryProperties = retryProperties;
    maxRetry = retryProperties.getDelayChannels().size();
    this.streamBridge = streamBridge;
  }

  public <T> MessageBuilder<T> delayMessage(Message<T> message) {
    final int retryCount = getRetryCount(message);
    final MessageBuilder<T> retryMessageBuilder = retryMessageBuilder(message);
    if (retryCount < maxRetry) {
      retryMessageBuilder.setHeader(SPRING_CLOUD_STREAM_SENDTO_HEADER, getRetryDestinationName(retryCount));
    } else {
      retryMessageBuilder.setHeader(SPRING_CLOUD_STREAM_SENDTO_HEADER, retryProperties.getParkingDestinationName());
    }
    return retryMessageBuilder;
  }

  public <T> void sendRetry(Message<T> message) {
    final int retryCount = getRetryCount(message);
    if (retryCount < maxRetry) {
      final Message<T> retryMessage = retryMessageBuilder(message).build();
      streamBridge.send(getRetryDestinationName(retryCount), retryMessage);
    } else {
      streamBridge.send(retryProperties.getParkingDestinationName(), message);
    }
  }

  private Integer getRetryCount(Message<?> message) {
    return (Integer) message.getHeaders().getOrDefault(retryProperties.getRetryCountHeader(), 0);
  }

  private String getRetryDestinationName(int retryCount) {
    return retryProperties.getDelayChannels().get(DELAY_CHANNEL_PREFIX + (retryCount + 1)).getDestinationName();
  }

  private <T> MessageBuilder<T> retryMessageBuilder(Message<T> message) {
    final int retryCount = getRetryCount(message);
    return MessageBuilder.fromMessage(message)
        .setHeader(retryProperties.getRetryCountHeader(), retryCount + 1)
        .setHeader(retryProperties.getRetryDestinationHeader(), retryProperties.getInputExchangeName());
  }
}
