package org.sylvl.retryrabbit;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitControl {
  private final AmqpAdmin amqpAdmin;
  private final RetryProperties retryProperties;
  private final String parkingGroupName;
  private final String inputGroupName;

  public RabbitControl(AmqpAdmin amqpAdmin, RetryProperties retryProperties,
                       @Value("${retryrabbit.parking-destination-name}") String parkingGroupName,
                       @Value("${retryrabbit.input-exchange-name}") String inputGroupName
  ) {
    this.amqpAdmin = amqpAdmin;
    this.retryProperties = retryProperties;
    this.parkingGroupName = parkingGroupName;
    this.inputGroupName = inputGroupName;
  }

  public void cleanUp() {
    amqpAdmin.deleteExchange(retryProperties.getInputExchangeName());
    amqpAdmin.deleteExchange(retryProperties.getRouterExchangeName());
    amqpAdmin.deleteExchange(retryProperties.getParkingDestinationName());
    retryProperties.getDelayChannels().forEach((delayChannelName, delayChannelProperties) -> {
      amqpAdmin.deleteQueue(delayChannelProperties.getDestinationName() + "." + retryProperties.getDelayGroupName());
      amqpAdmin.deleteExchange(delayChannelProperties.getDestinationName());
    });
    amqpAdmin.deleteQueue(retryProperties.getParkingDestinationName() + "." + parkingGroupName);
    amqpAdmin.deleteQueue(retryProperties.getInputExchangeName() + "." + inputGroupName);
  }
}
