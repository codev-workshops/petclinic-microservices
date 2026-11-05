package org.springframework.samples.petclinic.api.dto;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DtoTest {
    @Test void ownerBuilderRetainsFieldsAndPetIds() {
        var pet = PetDetails.PetDetailsBuilder.aPetDetails().id(2).name("Cat")
            .birthDate("2020-01-01").type(new PetType("cat")).visits(List.of()).build();
        var owner = OwnerDetails.OwnerDetailsBuilder.anOwnerDetails().id(1).firstName("Jane")
            .lastName("Doe").address("Road").city("Town").telephone("123").pets(List.of(pet)).build();
        assertThat(owner.id()).isEqualTo(1);
        assertThat(owner.firstName()).isEqualTo("Jane");
        assertThat(owner.lastName()).isEqualTo("Doe");
        assertThat(owner.address()).isEqualTo("Road");
        assertThat(owner.city()).isEqualTo("Town");
        assertThat(owner.telephone()).isEqualTo("123");
        assertThat(owner.getPetIds()).containsExactly(2);
        assertThat(pet.name()).isEqualTo("Cat");
        assertThat(pet.birthDate()).isEqualTo("2020-01-01");
        assertThat(pet.type().name()).isEqualTo("cat");
        assertThat(pet.visits()).isEmpty();
    }
    @Test void nullVisitsBecomeMutableEmptyList() {
        var pet = new PetDetails(1, "Cat", null, null, null);
        pet.visits().add(new VisitDetails(1, 1, null, "checkup"));
        assertThat(pet.visits()).hasSize(1);
    }
    @Test void defaultVisitsAreEmpty() {
        assertThat(new Visits().items()).isEmpty();
    }
}
