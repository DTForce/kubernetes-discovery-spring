# kubernetes-discovery-spring

[![Build Status](https://travis-ci.org/DTForce/kubernetes-discovery-spring.svg?branch=master)](https://travis-ci.org/DTForce/kubernetes-discovery-spring)

Kubernetes Service Discovery for Spring Cloud and Ribbon

Compatible with __Spring Boot 2.1.0__ and __Spring Cloud Greenwich__

## Overview

This library provides implementations of Spring Cloud's `DiscoveryClient` class and
Ribbon's `ServerList`. It is compatible with Spring Cloud Greenwich and is meant
to be a very thin integration layer between Kubernetes and Spring Cloud + Ribbon.

Compared to other integrations, this one still leaves the actual load balancing on
kubernetes. Its purpose is discover to services and provide seamless transition between
other Ribbon providers like Eureka. That means, that you can still use `@LoadBalanced 
RestTemplate` while avoiding problems with stale Pod IPs during rolling update of a 
deployment or other changes to Service-to-Pod mapping in Kubernetes. 

Under the hood, the library makes use of fabric8's `kubernetes-client` Java library to communicate
with the Kubernetes cluster.

*Made with :heart: in Prague by [DT>Force](http://www.dtforce.com/).*

## Quick Start

### Maven

```xml
<dependencies>
    <dependency>
        <groupId>com.dtforce</groupId>
        <artifactId>kubernetes-discovery-spring</artifactId>
        <version>1.1.0</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
dependencies {
    compile 'com.dtforce:kubernetes-discovery-spring:1.1.0'
}
```

## Label-based filtering

Since version 1.1.0, the Kubernetes Discovery Client implements `SelectorEnabledDiscoveryClient`, a new interface of
our own making. This interface, a subset of the vanilla `DiscoveryClient`, allows developers to go beyong traditional
name-based instance discovery by providing methods to get instances based on their metadata.

In `kubernetes-discovery-spring`, the selector-enabled Discovery Client implementation filters services
by equality-based Label matching. The interface methods can be provided with labels to match and labels to not match.

## Configuration

### Kubernetes API credentials

Because `kubernetes-discovery-spring` uses the great `kubernetes-client` library
from fabric8.io, when running in a Pod no special configuration is needed to
establish communications with Kubernetes' API. A default Service Account token and CA are
automatically mounted as files in K8S containers and picked up by `kubernetes-client`.

It can also get API credentials from `kubectl` configuration files, Spring config properties and environment variables.
See the official documentation of `kubernetes-client` here for more details: https://github.com/fabric8io/kubernetes-client

### Spring Configuration properties

 * `spring.cloud.kubernetes.enabled` (boolean) : Enable/disable the whole library integration - if set to true, bean of 
 `KubernetesClient`(fabric8) is registered, if one does not exist. (default=false)
 * `spring.cloud.kubernetes.discovery.enabled` (boolean) : Enable/disable Spring Cloud Discovery integration. (default=false)
 * `spring.cloud.kubernetes.ribbon.enabled` (boolean) : Enable/disable Ribbon integration. (default=false)

## License

This code is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).
