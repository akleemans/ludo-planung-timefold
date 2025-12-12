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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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

        s += "\nPeople  (for copy-paste)\n===================\n";
        for (Shift shift : shifts) {
            s += shift.getPersonA() + ", " + shift.getPersonB() + "\n";
        }

        s += "\nPeople details\n===================\n";
        for (Person person : people) {
            List<LocalDate> dateList = new ArrayList<>();
            for (Shift shift : shifts) {
                if (shift.getPersonA() != null && shift.getPersonA().equals(person) || shift.getPersonB() != null && shift.getPersonB().equals(person)) {
                    dateList.add(shift.getDate());
                }
            }
            long maxGap =
                    IntStream.range(0, dateList.size() - 1)
                            .mapToLong(i -> ChronoUnit.DAYS.between(dateList.get(i), dateList.get(i + 1)))
                            .max()
                            .orElse(0);

            long minGap =
                    IntStream.range(0, dateList.size() - 1)
                            .mapToLong(i -> ChronoUnit.DAYS.between(dateList.get(i), dateList.get(i + 1)))
                            .min()
                            .orElse(0);
            // TODO also log amount of unwanted dates
            s += person.getName() + ", shifts: " + dateList.size() + "/" + person.getIdealLoad() * PLANNING_MONTHS + ", min/max gaps: "
                    + minGap + "/" + maxGap + " (ideal: " + Util.getExpectedGapDays(person) + ") " + ", dates: " + dateList + "\n";
        }
        return s;
    }
}
