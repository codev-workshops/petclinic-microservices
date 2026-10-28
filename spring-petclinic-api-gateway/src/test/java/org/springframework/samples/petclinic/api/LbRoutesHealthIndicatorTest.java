package org.springframework.samples.petclinic.api;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LbRoutesHealthIndicatorTest {

    @Test
    void reportsUpWhenEveryLoadBalancedServiceHasInstances() {
        RouteDefinitionLocator routes = mock(RouteDefinitionLocator.class);
        ReactiveDiscoveryClient discovery = mock(ReactiveDiscoveryClient.class);
        when(routes.getRouteDefinitions()).thenReturn(Flux.just(
            route("lb://customers-service"),
            route("lb://customers-service"),
            route("lb://vets-service")));
        when(discovery.getInstances("customers-service"))
            .thenReturn(Flux.just(mock(ServiceInstance.class), mock(ServiceInstance.class)));
        when(discovery.getInstances("vets-service")).thenReturn(Flux.just(mock(ServiceInstance.class)));
        LbRoutesHealthIndicator indicator = new LbRoutesHealthIndicator(routes, discovery);

        StepVerifier.create(indicator.health())
            .assertNext(health -> {
                assertEquals(Status.UP, health.getStatus());
                assertEquals(
                    Map.of("customers-service", 2L, "vets-service", 1L),
                    health.getDetails().get("instances"));
                assertEquals(List.of(), health.getDetails().get("missing"));
            })
            .verifyComplete();
        verify(discovery, times(1)).getInstances("customers-service");
    }

    @Test
    void reportsOutOfServiceAndListsMissingServices() {
        RouteDefinitionLocator routes = mock(RouteDefinitionLocator.class);
        ReactiveDiscoveryClient discovery = mock(ReactiveDiscoveryClient.class);
        when(routes.getRouteDefinitions()).thenReturn(Flux.just(
            route("lb://customers-service"),
            route("lb://visits-service")));
        when(discovery.getInstances("customers-service")).thenReturn(Flux.just(mock(ServiceInstance.class)));
        when(discovery.getInstances("visits-service")).thenReturn(Flux.empty());
        LbRoutesHealthIndicator indicator = new LbRoutesHealthIndicator(routes, discovery);

        StepVerifier.create(indicator.health())
            .assertNext(health -> {
                assertEquals(Status.OUT_OF_SERVICE, health.getStatus());
                assertEquals(
                    Map.of("customers-service", 1L, "visits-service", 0L),
                    health.getDetails().get("instances"));
                assertEquals(List.of("visits-service"), health.getDetails().get("missing"));
            })
            .verifyComplete();
    }

    @Test
    void ignoresNonLoadBalancedRoutes() {
        RouteDefinitionLocator routes = mock(RouteDefinitionLocator.class);
        ReactiveDiscoveryClient discovery = mock(ReactiveDiscoveryClient.class);
        when(routes.getRouteDefinitions()).thenReturn(Flux.just(route("https://example.org")));
        LbRoutesHealthIndicator indicator = new LbRoutesHealthIndicator(routes, discovery);

        StepVerifier.create(indicator.health())
            .assertNext(health -> {
                assertEquals(Status.UP, health.getStatus());
                assertEquals(Map.of(), health.getDetails().get("instances"));
                assertEquals(List.of(), health.getDetails().get("missing"));
            })
            .verifyComplete();
        verifyNoInteractions(discovery);
    }

    private RouteDefinition route(String uri) {
        RouteDefinition route = new RouteDefinition();
        route.setUri(URI.create(uri));
        return route;
    }
}
