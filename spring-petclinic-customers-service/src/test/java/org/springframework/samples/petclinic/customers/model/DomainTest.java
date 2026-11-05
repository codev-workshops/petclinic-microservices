package org.springframework.samples.petclinic.customers.model;

import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

class DomainTest {
    @Test void petsAreSortedAndImmutable() {
        Owner owner = new Owner();
        assertThat(owner.getPets()).isEmpty();
        Pet zebra = new Pet(); zebra.setName("Zebra");
        Pet amy = new Pet(); amy.setName("Amy");
        owner.addPet(zebra); owner.addPet(amy);
        assertThat(owner.getPets()).containsExactly(amy, zebra);
        assertThat(amy.getOwner()).isSameAs(owner);
        assertThatThrownBy(() -> owner.getPets().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
    private Pet pet() {
        Pet pet = new Pet();
        pet.setId(1); pet.setName("Cat"); pet.setBirthDate(new Date(0));
        return pet;
    }
    @Test void matchingPetsHaveEqualHashCodes() {
        assertThat(pet()).isEqualTo(pet()).hasSameHashCodeAs(pet());
    }
    @Test void nullAndOtherTypesAreNotEqual() {
        assertThat(pet().equals(null)).isFalse();
        assertThat(pet().equals("cat")).isFalse();
    }
    @ParameterizedTest @ValueSource(strings={"id", "name", "date", "type", "owner"})
    void differingFieldsAreNotEqual(String field) {
        Pet other = pet();
        switch (field) {
            case "id" -> other.setId(2);
            case "name" -> other.setName("Dog");
            case "date" -> other.setBirthDate(new Date(1));
            case "type" -> other.setType(new PetType());
            case "owner" -> other.setOwner(new Owner());
        }
        assertThat(pet()).isNotEqualTo(other);
    }
    @Test void petDescriptionIncludesOwnerAndType() {
        Pet pet = pet(); PetType type = new PetType(); type.setName("cat");
        Owner owner = new Owner(); owner.setFirstName("Jane"); owner.setLastName("Doe");
        pet.setType(type); pet.setOwner(owner);
        assertThat(pet.toString()).contains("Cat", "Jane", "Doe", "cat");
        assertThat(type.getId()).isNull();
        assertThat(type.getName()).isEqualTo("cat");
    }
}
