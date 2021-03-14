package org.sylvl.retryrabbit;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;
import com.rabbitmq.http.client.domain.BindingInfo;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import com.rabbitmq.http.client.domain.QueueInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.junit.RabbitAvailable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

@TestPropertySource("/test-delay.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RabbitAvailable
@SpringBootTest(properties =
    {"spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration"})
public class RetryWithRabbitTests {

  private final Client rabbitClient;

  @Autowired
  private RabbitAdmin rabbitAdmin;

  @Autowired
  private RabbitControl rabbitControl;

  @Autowired
  private RetryProperties retryProperties;

  public RetryWithRabbitTests() throws MalformedURLException, URISyntaxException {
    rabbitClient = new Client(
        new ClientParameters()
            .url("http://127.0.0.1:15672/api/")
            .username("guest")
            .password("guest")
    );
  }

  @BeforeAll
  void init() {
    rabbitAdmin.initialize();
  }

  @Test
  void rabbitInfraShouldBeSetUp() {
  }

  @Test
  void retryRouter() {
    final ExchangeInfo routerExchange = rabbitClient.getExchange("/", retryProperties.getRouterExchangeName());
    assertThat("router exchange should exist", routerExchange != null);
    assertThat("router exchange should exist", routerExchange.getType().equals("headers"));
    final BindingInfo retryRouterBinding = rabbitClient.getBindingsBySource("/", "retry_router").get(0);
    assertThat("destination should be input exchange",
        retryRouterBinding.getDestination().equals(retryProperties.getInputExchangeName()));
    assertThat("routing header should be set",
        retryRouterBinding.getArguments().get(retryProperties.getRetryDestinationHeader()).equals(retryProperties.getInputExchangeName()));
  }

  @Test
  void delayQueues() {
    retryProperties.getDelayChannels().forEach((delayName, delayProps) -> {
      assertThat("delay exchange " + delayProps.getDestinationName() + " should exist", rabbitClient.getExchange("/", delayProps.getDestinationName()) != null);
      final String queueName = delayProps.getDestinationName() + "." + retryProperties.getDelayGroupName();
      final QueueInfo queue = rabbitClient.getQueue("/", queueName);
      assertThat("queue should exist", queue != null);
      final Map<String, Object> arguments = queue.getArguments();
      assertThat("dead letter exchange should be set", arguments.get("x-dead-letter-exchange").equals(retryProperties.getRouterExchangeName()));
      assertThat("message ttl should be set", arguments.get("x-message-ttl").equals(delayProps.getWaitingTime()));
    });
  }

  @AfterAll
  public void afterAll() {
    rabbitControl.cleanUp();
  }

}
