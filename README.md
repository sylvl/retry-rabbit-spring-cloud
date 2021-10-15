# retry-rabbit-spring-cloud
Retry mechanism with Rabbit Mq and Spring Cloud Stream

## What for?

In message oriented architectures, you probably have to handle failures of external services and have a way to replay a message.

When a rest query fails, you may first want to wait 10 seconds before retrying, and each 
time it fails, you may want to wait longer (1 minute, 1 hour, ...).

This library  

## Features
* Easy set up of rabbit mq based retry infrastructure
* Allows setting up an arbitrary number of waiting queues (configurable through properties)
* Multiple services can share the same waiting queues
* Only requires Rabbit Mq as backing infrastructure (no need for a database)

## Installation

add dependency, 
with Maven:
`<dependency>
<groupId>org.sylvl</groupId>
<artifactId>retry-rabbit-spring-cloud</artifactId>
<version>0.0.1-SNAPSHOT</version>
</dependency>`

with Gradle:
`implementation 'org.sylvl:retry-rabbit-spring-cloud:0.0.1-SNAPSHOT'`

## Usage

