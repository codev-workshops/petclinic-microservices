package org.springframework.samples.petclinic.visits.web;

import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class VisitResourceUnitTest {
    private final VisitRepository repository = mock(VisitRepository.class);
    private final VisitResource resource = new VisitResource(repository);
    @Test void createsVisitForPathPet() {
        var date = new Date(0);
        var visit = Visit.VisitBuilder.aVisit().id(1).date(date).description("checkup").petId(2).build();
        when(repository.save(visit)).thenReturn(visit);
        assertThat(resource.create(visit, 3)).isSameAs(visit);
        assertThat(visit.getPetId()).isEqualTo(3);
        assertThat(visit.getId()).isEqualTo(1);
        assertThat(visit.getDate()).isEqualTo(date);
        assertThat(visit.getDescription()).isEqualTo("checkup");
    }
    @Test void readsVisitsForPet() {
        var visits = List.of(new Visit());
        when(repository.findByPetId(1)).thenReturn(visits);
        assertThat(resource.read(1)).isSameAs(visits);
    }
    @Test void readsEmptyBatch() {
        assertThat(resource.read(List.of()).items()).isEmpty();
    }
    @Test void readsBatch() {
        var visits = List.of(new Visit());
        when(repository.findByPetIdIn(List.of(1, 2))).thenReturn(visits);
        assertThat(resource.read(List.of(1, 2)).items()).isSameAs(visits);
    }
    @Test void propagatesReadFailure() {
        when(repository.findByPetId(1)).thenThrow(new IllegalStateException("offline"));
        assertThatThrownBy(() -> resource.read(1)).hasMessage("offline");
    }
}
