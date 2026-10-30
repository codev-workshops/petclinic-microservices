package org.springframework.samples.petclinic.api;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component("lbRoutes")
public class LbRoutesHealthIndicator implements ReactiveHealthIndicator {

    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(3);

    private final RouteDefinitionLocator routeDefinitionLocator;
    private final ReactiveDiscoveryClient discoveryClient;

    public LbRoutesHealthIndicator(RouteDefinitionLocator routeDefinitionLocator,
                                   ReactiveDiscoveryClient discoveryClient) {
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Mono<Health> health() {
        return routeDefinitionLocator.getRouteDefinitions()
            .filter(route -> "lb".equalsIgnoreCase(route.getUri().getScheme()))
            .map(RouteDefinition::getUri)
            .map(uri -> uri.getHost())
            .filter(serviceId -> serviceId != null && !serviceId.isBlank())
            .distinct()
            .collectList()
            .flatMapMany(Flux::fromIterable)
            .flatMap(serviceId -> discoveryClient.getInstances(serviceId)
                .count()
                .map(count -> Map.entry(serviceId, count)))
            .collectMap(Map.Entry::getKey, Map.Entry::getValue)
            .map(this::healthForInstances)
            .timeout(HEALTH_CHECK_TIMEOUT)
            .onErrorResume(error -> Mono.just(Health.down(error).build()));
    }

    private Health healthForInstances(Map<String, Long> instances) {
        List<String> missing = instances.entrySet().stream()
            .filter(entry -> entry.getValue() == 0)
            .map(Map.Entry::getKey)
            .toList();
        Health.Builder health = missing.isEmpty() ? Health.up() : Health.outOfService();
        return health
            .withDetail("instances", instances)
            .withDetail("missing", missing)
            .build();
    }
}
