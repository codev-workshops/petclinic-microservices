package org.springframework.samples.petclinic.customers.web;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PetResourceUnitTest {
    private final PetRepository pets = mock(PetRepository.class);
    private final OwnerRepository owners = mock(OwnerRepository.class);
    private final PetResource resource = new PetResource(pets, owners);
    private final PetRequest request = new PetRequest(2, new Date(0), "Basil", 6);

    @Test void listsTypes() {
        var types = List.of(new PetType());
        when(pets.findPetTypes()).thenReturn(types);
        assertThat(resource.getPetTypes()).isSameAs(types);
    }
    @Test void emptyTypesAreReturned() {
        assertThat(resource.getPetTypes()).isEmpty();
    }
    @Test void createsAndAssociatesPet() {
        var owner = new Owner(); var type = new PetType(); type.setName("cat");
        when(owners.findById(1)).thenReturn(Optional.of(owner));
        when(pets.findPetTypeById(6)).thenReturn(Optional.of(type));
        when(pets.save(any())).thenAnswer(call -> call.getArgument(0));
        var pet = resource.processCreationForm(request, 1);
        assertThat(owner.getPets()).containsExactly(pet);
        assertThat(pet.getName()).isEqualTo("Basil");
        assertThat(pet.getBirthDate()).isEqualTo(new Date(0));
        assertThat(pet.getType()).isSameAs(type);
    }
    @Test void missingOwnerPreventsCreation() {
        assertThatThrownBy(() -> resource.processCreationForm(request, 1))
            .isInstanceOf(ResourceNotFoundException.class).hasMessage("Owner 1 not found");
        verify(pets, never()).save(any());
    }
    @Test void updatesPet() {
        var pet = new Pet(); pet.setOwner(new Owner());
        var type = new PetType(); type.setName("cat"); pet.setType(type);
        when(pets.findById(2)).thenReturn(Optional.of(pet));
        resource.processUpdateForm(request);
        assertThat(pet.getName()).isEqualTo("Basil");
        assertThat(pet.getType()).isSameAs(type);
        verify(pets).save(pet);
    }
    @Test void missingPetPreventsUpdate() {
        assertThatThrownBy(() -> resource.processUpdateForm(request))
            .isInstanceOf(ResourceNotFoundException.class).hasMessage("Pet 2 not found");
    }
    @Test void missingPetCannotBeRead() {
        assertThatThrownBy(() -> resource.findPet(2))
            .isInstanceOf(ResourceNotFoundException.class).hasMessage("Pet 2 not found");
    }
}
