package org.springframework.samples.petclinic.genai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PetclinicChatClientTest {
    private final ChatClient client = mock(ChatClient.class, RETURNS_DEEP_STUBS);
    private PetclinicChatClient controller() {
        var builder = mock(ChatClient.Builder.class, RETURNS_SELF);
        when(builder.build()).thenReturn(client);
        return new PetclinicChatClient(builder, mock(ChatMemory.class), mock(PetclinicTools.class));
    }
    @Test void returnsChatResponse() {
        when(client.prompt().user("hello").call().content()).thenReturn("Hi");
        assertThat(controller().exchange("hello")).isEqualTo("Hi");
    }
    @Test void emptyResponseIsPreserved() {
        when(client.prompt().user("").call().content()).thenReturn("");
        assertThat(controller().exchange("")).isEmpty();
    }
    @Test void failureReturnsFriendlyMessage() {
        when(client.prompt()).thenThrow(new IllegalStateException("offline"));
        assertThat(controller().exchange("hello"))
            .isEqualTo("Chat is currently unavailable. Please try again later.");
    }
}
