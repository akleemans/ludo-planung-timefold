package ch.kleemans.ludoplanung.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
public class Person {
    @PlanningId
    private String name;
    private float idealLoad;
    private Set<LocalDate> availableDates;

    public boolean isAvailable(LocalDate date) {
        return availableDates.contains(date);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Person person)) {
            return false;
        }
        return Objects.equals(getName(), person.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
