package org.springframework.samples.petclinic.genai;

import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.genai.dto.*;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class VectorStoreControllerTest {
    private WebClient.Builder builder() {
        return WebClient.builder().exchangeFunction(request -> {
            assertThat(request.url().toString()).isEqualTo("http://vets-service/vets");
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("[{\"id\":1,\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"specialties\":[]}]").build());
        });
    }
    @Test void loadsBundledVectorStoreWithoutNetwork() throws Exception {
        var store = mock(SimpleVectorStore.class);
        new VectorStoreController(store, WebClient.builder()).loadVetDataToVectorStoreOnStartup(null);
        verify(store).load(any(java.io.File.class));
        verify(store, never()).add(anyList());
    }
    @Test void fetchesAndEmbedsWhenNoBundledStoreExists() throws Exception {
        var store = mock(VectorStore.class);
        try (var resources = mockConstruction(ClassPathResource.class)) {
            new VectorStoreController(store, builder()).loadVetDataToVectorStoreOnStartup(null);
        }
        verify(store).add(argThat(documents -> documents.size() == 1));
    }
    @Test void savesGeneratedSimpleStore() throws Exception {
        var store = mock(SimpleVectorStore.class);
        doAnswer(call -> {
            var file = call.getArgument(0, java.io.File.class);
            assertThat(Files.getPosixFilePermissions(file.toPath())).containsExactlyInAnyOrder(
                java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                java.nio.file.attribute.PosixFilePermission.OWNER_WRITE,
                java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE);
            Files.delete(file.toPath());
            return null;
        }).when(store).save(any(java.io.File.class));
        try (var resources = mockConstruction(ClassPathResource.class)) {
            new VectorStoreController(store, builder()).loadVetDataToVectorStoreOnStartup(null);
        }
        verify(store).save(any(java.io.File.class));
    }
    @Test void serializesVetList() throws Exception {
        var vet = new Vet(1, "Jane", "Doe", Set.of(new Specialty(2, "surgery")));
        var resource = new VectorStoreController(mock(VectorStore.class), builder())
            .convertListToJsonResource(List.of(vet));
        assertThat(resource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
            .contains("Jane", "surgery");
    }
    @Test void serializesEmptyList() throws Exception {
        var resource = new VectorStoreController(mock(VectorStore.class), builder())
            .convertListToJsonResource(List.of());
        assertThat(resource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).isEqualTo("[]");
    }
    @Test void serializationFailureReturnsNull() {
        try (var mappers = mockConstruction(ObjectMapper.class, (mapper, context) ->
            when(mapper.writeValueAsString(any())).thenThrow(mock(JacksonException.class)))) {
            assertThat(new VectorStoreController(mock(VectorStore.class), builder())
                .convertListToJsonResource(List.of())).isNull();
        }
    }
}
