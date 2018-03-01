# kubernetes-discovery-spring

[![Build Status](https://travis-ci.org/DTForce/kubernetes-discovery-spring.svg?branch=master)](https://travis-ci.org/DTForce/kubernetes-discovery-spring)

Kubernetes Service Discovery for Spring Cloud and Ribbon

## Overview

This library provides implementations of Spring Cloud's `DiscoveryClient` class and
Ribbon's `ServerList`. It is compatible with Spring Cloud 2.0 and above, and is meant
to be a very thin integration layer between Kubernetes and Spring Cloud + Ribbon.

Under the hood, it makes use of fabric8's `kubernetes-client` Java library to communicate
with the Kubernetes cluster.

*Made with :heart: in Prague by [DT>Force](http://www.dtforce.com/).*

## Quick Start

### Maven

```xml
<dependencies>
    <dependency>
        <groupId>com.dtforce</groupId>
        <artifactId>kubernetes-discovery-spring</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
<repositories>
    <repository>
        <id>spring-milestones</id>
        <name>Spring Milestones</name>
        <url>https://repo.spring.io/libs-milestone</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

### Gradle

```groovy
dependencies {
    compile 'com.dtforce:kubernetes-discovery-spring:1.0.0'
}
repositories {
    maven {
        url 'https://repo.spring.io/libs-milestone'
    }
}
```

## Configuration

### Kubernetes API credentials

Because `kubernetes-discovery-spring` uses the great `kubernetes-client` library
from fabric8.io, when running in a Pod no special configuration is needed to
establish communications with Kubernetes' API. A default Service Account token and CA are
automatically mounted as files in containers spawned in Kubernetes.

It can also get API credentials from `kubectl` configuration files, Spring config properties and environment variables.
See the official documentation of `kubernetes-client` here for more details: https://github.com/fabric8io/kubernetes-client

### Spring Configuration properties

 * `spring.kubernetes.discovery.enabled` (boolean) : Enable/disable Spring Cloud Discovery integration. Enabled by default
 * `spring.kubernetes.ribbon.enabled` (boolean) : Enable/disable Ribbon integration. Enabled by default

## License

This code is licensed under the [Apache License 2.0]().