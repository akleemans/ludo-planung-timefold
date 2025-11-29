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

import java.util.List;

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
        var s = "";
        for (Shift shift : shifts) {
            s += shift.toString() + "\n";
        }
        return s;
    }
}
