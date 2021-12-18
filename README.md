# retry-rabbit-spring-cloud
Retry mechanism with Rabbit Mq and Spring Cloud Stream

## What for?

In message oriented architectures, you probably have to handle failures of external services and have a way to replay a message.

On the first failure, you may want to wait 10 seconds before retrying, and each 
time it fails, you may want to wait longer (1 minute, 1 hour, ...).

This library makes use of Rabbit Mq TTL queues to provide a retry mechanism.

## Features
* Easy set up of rabbit mq based retry infrastructure
* Allows setting up an arbitrary number of waiting queues (configurable through properties)
* Multiple services can share the same waiting queues
* Only requires Rabbit Mq as backing infrastructure (no need for a database)

Note that there would be other ways to achieve the same goal.
The takeover is that this implementation doesn't require any other component than Rabbit Mq and can be fine-tuned to handle multiple waiting durations

## Installation

add dependency, 
with Maven:
```xml
<dependency>
<groupId>org.sylvl</groupId>
<artifactId>retry-rabbit-spring-cloud</artifactId>
<version>0.0.1-SNAPSHOT</version>
</dependency>
```

with Gradle:
```
implementation 'org.sylvl:retry-rabbit-spring-cloud:0.0.1-SNAPSHOT'
```

## Usage

### configure

Set up your Spring Cloud bindings:

```properties
# main function bindings
spring.cloud.stream.binders.example-rabbit.type=rabbit
spring.cloud.stream.bindings.process-in-0.destination=example
spring.cloud.stream.bindings.process-in-0.group=default
spring.cloud.stream.bindings.process-in-0.binder=example-rabbit
spring.cloud.stream.bindings.process-out-0.destination=success
spring.cloud.stream.bindings.process-out-0.producer.required-groups=default
spring.cloud.stream.bindings.process-out-0.binder=example-rabbit
# error binding
spring.cloud.stream.bindings.parking.destination=parking
spring.cloud.stream.bindings.parking.producer.required-groups=default
spring.cloud.stream.bindings.parking.binder=example-rabbit

```

Retry Rabbit specific parameters:
```properties
# Exchange where messages are routed after ttl expiration
retry-rabbit.router-exchange-name=retry_router

# Exchange where messages are routed after each retry
retry-rabbit.input-exchange-name=${spring.cloud.stream.bindings.process-in-0.destination}

# Exchange where messages are routed after reaching the maximum number of retries
retry-rabbit.parking-destination-name=${spring.cloud.stream.bindings.parking.destination}

# retry destinations
retry-rabbit.delay-group-name=default
retry-rabbit.delay-channels.delay1.waiting-time=1000
retry-rabbit.delay-channels.delay1.destination-name=delay1
retry-rabbit.delay-channels.delay2.waiting-time=10000
retry-rabbit.delay-channels.delay2.destination-name=delay2
# ...
# you can add as many retry destinations as needed

```

Add the retry router to your code, in a function:
```java
return retryRouter.delayMessage(message).build();
```
in a consumer:
```java
retryRouter.sendRetry(message);
```

example:
```java
@Configuration
public class ExampleProcessor {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleProcessor.class);

  private final RetryRouter retryRouter;

  public ExampleProcessor(RetryRouter retryRouter) {
    this.retryRouter = retryRouter;
  }

  @Bean
  public Function<Message<String>, Message<String>> process() {
    return (Message<String> m) -> {
      LOGGER.info("processing payload : " + m.getPayload());
      if (m.getPayload().contains("retry")) {
        return retryRouter.delayMessage(m).build();
      }
      LOGGER.info("success");
      return MessageBuilder.withPayload("done " + m.getPayload()).build();
    };
  }
}
```

## How it works

Set up
- an exchange/queue pair is created per waiting time
- a _parking_ exchange/queue pair is created, this is where messages will end up once they reached the maximum number of retries
- a _router_ exchange is created with the `headers` type, this is where messages will land once their TTL expires in the delay queues

At runtime
1. a message is sent to the input destination of the Spring Cloud function
2. when a retry is needed, it is sent to the first delay exchange
3. once the TTL expires, the message is routed to the _router_ exchange and sent back to the input destination
4. steps 2 and 3 for each waiting duration
5. if the message reaches the maximum retry count, it is sent to the _parking_ destination
6. if no retry is needed, the message can continue its normal route to the destination outpu