package ch.kleemans.ludoplanung.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PersonTest {
    private final LocalDate availableDate = LocalDate.of(2026, 2, 10);
    private final LocalDate unavailableDate = LocalDate.of(2026, 2, 13);

    private final Person alice = new Person("Alice", 1.5f, Set.of(availableDate));

    @Test
    void getIdealLoad() {
        assertThat(alice.getIdealLoad()).isEqualTo(1.5f);
    }

    @Test
    void isAvailable() {
        assertThat(alice.isAvailable(availableDate)).isTrue();
        assertThat(alice.isAvailable(unavailableDate)).isFalse();
    }
}
