package ch.kleemans.ludoplanung.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import ai.timefold.solver.core.api.solver.SolverStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static ch.kleemans.ludoplanung.LudoConstraintProvider.PLANNING_MONTHS;

@PlanningSolution
@NoArgsConstructor
@Getter
@Setter
public class LudoSchedule {

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Person> people;

    @PlanningEntityCollectionProperty
    private List<Shift> shifts;

    @PlanningScore
    private HardSoftBigDecimalScore score;

    private SolverStatus solverStatus;

    public LudoSchedule(List<Shift> shifts, List<Person> people) {
        this.shifts = shifts;
        this.people = people;
    }

    @Override
    public String toString() {
        var s = "Shifts\n===================\n";
        for (Shift shift : shifts) {
            s += shift.toString() + "\n";
        }

        s += "\nPeople\n===================\n";
        for (Person person : people) {
            List<LocalDate> shiftList = new ArrayList<>();
            for (Shift shift : shifts) {
                if (shift.getPersonA() != null && shift.getPersonA().equals(person) || shift.getPersonB() != null && shift.getPersonB().equals(person)) {
                    shiftList.add(shift.getDate());
                }
            }
            s += person.getName() + ": " + shiftList + ", shifts: " +shiftList.size() + "/" + person.getIdealLoad() * PLANNING_MONTHS + "\n";
        }
        return s;
    }
}
