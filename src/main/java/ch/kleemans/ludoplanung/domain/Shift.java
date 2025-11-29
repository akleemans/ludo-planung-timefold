package ch.kleemans.ludoplanung.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Objects;

@PlanningEntity
@Getter
@Setter
@NoArgsConstructor
public class Shift {
    @PlanningId
    private String id;

    private LocalDate date;

    @PlanningVariable
    private Person personA;

    @PlanningVariable
    private Person personB;

    public Shift(String date) {
        this.id = date;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        this.date = LocalDate.parse(date, formatter);
    }

    public Shift(LocalDate date, Person personA, Person personB) {
        this.id = date.toString();
        this.date = date;
        this.personA = personA;
        this.personB = personB;
    }

    public Shift(String dateString, Person personA, Person personB) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        this.id = dateString;
        this.date = LocalDate.parse(dateString, formatter);
        this.personA = personA;
        this.personB = personB;
    }

    public int getWeekNumber() {
        return date.get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    @Override
    public String toString() {
        if (personA != null && personB != null) {
            return date + ": " + personA + ", " + personB;
        }
        return date.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Shift shift)) {
            return false;
        }
        return Objects.equals(getId(), shift.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
