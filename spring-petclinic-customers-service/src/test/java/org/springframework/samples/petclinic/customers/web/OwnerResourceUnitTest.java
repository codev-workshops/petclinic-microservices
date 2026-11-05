package org.springframework.samples.petclinic.customers.web;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OwnerResourceUnitTest {
    private final OwnerRepository repository = mock(OwnerRepository.class);
    private final OwnerResource resource = new OwnerResource(repository, new OwnerEntityMapper());
    private final OwnerRequest request = new OwnerRequest("Jane", "Doe", "Road", "Town", "0123456789");

    @Test void createsMappedOwner() {
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        Owner owner = resource.createOwner(request);
        assertThat(owner.getFirstName()).isEqualTo("Jane");
        assertThat(owner.getLastName()).isEqualTo("Doe");
        assertThat(owner.getAddress()).isEqualTo("Road");
        assertThat(owner.getCity()).isEqualTo("Town");
        assertThat(owner.getTelephone()).isEqualTo("0123456789");
    }
    @Test void findsOwner() {
        Owner owner = new Owner();
        when(repository.findById(1)).thenReturn(Optional.of(owner));
        assertThat(resource.findOwner(1)).contains(owner);
    }
    @Test void absentOwnerIsEmpty() {
        assertThat(resource.findOwner(1)).isEmpty();
    }
    @Test void listsOwners() {
        List<Owner> owners = List.of(new Owner());
        when(repository.findAll()).thenReturn(owners);
        assertThat(resource.findAll()).isSameAs(owners);
    }
    @Test void emptyRepositoryReturnsEmptyList() {
        assertThat(resource.findAll()).isEmpty();
    }
    @Test void updatesExistingOwner() {
        Owner owner = new Owner();
        when(repository.findById(1)).thenReturn(Optional.of(owner));
        resource.updateOwner(1, request);
        assertThat(owner.getFirstName()).isEqualTo("Jane");
        verify(repository).save(owner);
    }
    @Test void missingUpdateFailsWithoutSaving() {
        assertThatThrownBy(() -> resource.updateOwner(1, request))
            .isInstanceOf(ResourceNotFoundException.class).hasMessage("Owner 1 not found");
        verify(repository, never()).save(any());
    }
    @Test void repositoryFailurePropagates() {
        when(repository.findAll()).thenThrow(new IllegalStateException("offline"));
        assertThatThrownBy(resource::findAll).hasMessage("offline");
    }
}
