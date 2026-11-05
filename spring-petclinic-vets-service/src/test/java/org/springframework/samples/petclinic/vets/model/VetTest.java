package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class VetTest {
    @Test void retainsIdentity() {
        var vet = new Vet(); vet.setId(1); vet.setFirstName("Jane"); vet.setLastName("Doe");
        assertThat(vet.getId()).isEqualTo(1);
        assertThat(vet.getFirstName()).isEqualTo("Jane");
        assertThat(vet.getLastName()).isEqualTo("Doe");
    }
    @Test void startsWithNoSpecialties() {
        var vet = new Vet();
        assertThat(vet.getSpecialties()).isEmpty();
        assertThat(vet.getNrOfSpecialties()).isZero();
    }
    @Test void sortsSpecialtiesAndPreventsMutation() {
        var vet = new Vet();
        var surgery = new Specialty(); surgery.setName("surgery");
        var dentistry = new Specialty(); dentistry.setName("dentistry");
        vet.addSpecialty(surgery); vet.addSpecialty(dentistry);
        assertThat(vet.getSpecialties()).containsExactly(dentistry, surgery);
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
        assertThat(surgery.getId()).isNull();
        assertThatThrownBy(() -> vet.getSpecialties().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
