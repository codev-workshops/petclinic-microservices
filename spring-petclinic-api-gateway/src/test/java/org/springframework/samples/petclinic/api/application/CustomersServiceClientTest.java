package org.springframework.samples.petclinic.api.application;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import static org.assertj.core.api.Assertions.*;

class CustomersServiceClientTest {
    @Test void readsOwnerWithoutNetwork() {
        var builder = WebClient.builder().exchangeFunction(request -> {
            assertThat(request.url().toString()).isEqualTo("http://customers-service/owners/4");
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"id\":4,\"pets\":[]}").build());
        });
        assertThat(new CustomersServiceClient(builder).getOwner(4).block().id()).isEqualTo(4);
    }
    @Test void emptyResponseIsEmpty() {
        var builder = WebClient.builder().exchangeFunction(request ->
            Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build()));
        assertThat(new CustomersServiceClient(builder).getOwner(4).block()).isNull();
    }
    @Test void httpErrorPropagates() {
        var builder = WebClient.builder().exchangeFunction(request ->
            Mono.just(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE).build()));
        assertThatThrownBy(() -> new CustomersServiceClient(builder).getOwner(4).block())
            .isInstanceOf(WebClientResponseException.class);
    }
}
