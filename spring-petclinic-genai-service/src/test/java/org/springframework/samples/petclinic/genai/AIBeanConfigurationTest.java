package org.springframework.samples.petclinic.genai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIBeanConfigurationTest {
    @Test void createsVectorStoreWithoutCallingEmbeddingProvider() {
        var embedding = mock(EmbeddingModel.class);
        assertThat(new AIBeanConfiguration().vectorStore(embedding)).isInstanceOf(SimpleVectorStore.class);
        verifyNoInteractions(embedding);
    }
    @Test void createsWebClientBuilder() {
        assertThat(new AIBeanConfiguration().loadBalancedWebClientBuilder()).isNotNull();
    }
}
