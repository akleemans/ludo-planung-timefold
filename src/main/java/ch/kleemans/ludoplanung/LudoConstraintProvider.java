package ch.kleemans.ludoplanung;

import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.stream.*;
import ch.kleemans.ludoplanung.domain.Person;
import ch.kleemans.ludoplanung.domain.Shift;
import ch.kleemans.ludoplanung.domain.Util;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;

public class LudoConstraintProvider implements ConstraintProvider {

    public static float PLANNING_MONTHS = 5;

    @Override
    public Constraint[] defineConstraints(@NonNull ConstraintFactory constraintFactory) {
        return new Constraint[]{
                // Hard constraints
                personOnlyWhenAvailable(constraintFactory),
                // Implicit hard constraints
                noSamePersonTwiceInShift(constraintFactory),
                noPersonTwiceInSameWeek(constraintFactory),
                eachPersonPlannedAtLeastOnce(constraintFactory),

                // Soft constraints - strong
                idealMonthlyLoad(constraintFactory),

                // Soft constraints - weak
                wellDistributedShifts(constraintFactory),
                avoidUnwantedDates(constraintFactory),
        };
    }

    Constraint noSamePersonTwiceInShift(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(shift ->
                        shift.getPersonA() != null &&
                                shift.getPersonB() != null &&
                                shift.getPersonA().equals(shift.getPersonB())
                )
                .penalize(HardSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Person A and B must be different");
    }

    Constraint noPersonTwiceInSameWeek(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Shift.class, lessThan(Shift::getId))
                .filter((s1, s2) -> {
                    boolean samePerson =
                            Objects.equals(s1.getPersonA(), s2.getPersonA()) ||
                                    Objects.equals(s1.getPersonA(), s2.getPersonB()) ||
                                    Objects.equals(s1.getPersonB(), s2.getPersonA()) ||
                                    Objects.equals(s1.getPersonB(), s2.getPersonB());
                    boolean sameWeek = s1.getWeekNumber() == s2.getWeekNumber();
                    return samePerson && sameWeek;
                })
                .penalize(HardSoftBigDecimalScore.ONE_HARD)
                .asConstraint("No person twice in same week");
    }

    Constraint personOnlyWhenAvailable(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .filter(shift ->
                        !shift.getPersonA().isAvailable(shift.getDate())
                                || !shift.getPersonB().isAvailable(shift.getDate())
                )
                .penalize(HardSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Person must be available on shift date");
    }

    Constraint eachPersonPlannedAtLeastOnce(ConstraintFactory factory) {
        return factory.forEach(Person.class)
                .ifNotExists(Shift.class,
                        Joiners.filtering((person, shift) ->
                                Objects.equals(person, shift.getPersonA())
                                        || Objects.equals(person, shift.getPersonB())
                        )
                )
                .penalize(HardSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Each person must have at least one shift");
    }


    Constraint avoidUnwantedDates(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .filter(shift ->
                        shift.getPersonA().isUnwantedDate(shift.getDate())
                                || shift.getPersonB().isUnwantedDate(shift.getDate())
                )
                .penalize(HardSoftBigDecimalScore.ofSoft(BigDecimal.valueOf(0.2)))
                .asConstraint("Person should not work on unwanted day of week if possible");
    }

    Constraint idealMonthlyLoad(ConstraintFactory factory) {
        return factory.forEach(Person.class)
                .join(Shift.class,
                        // count shifts where the person appears as A or B
                        Joiners.filtering((person, shift) ->
                                Objects.equals(person, shift.getPersonA())
                                        || Objects.equals(person, shift.getPersonB())
                        )
                )
                // group by person, count how many shifts they have
                .groupBy(
                        (person, shift) -> person,
                        ConstraintCollectors.countBi()
                )
                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_SOFT,
                        (person, shiftCount) -> {
                            // ideal = idealLoad (per month) * 5 months
                            BigDecimal ideal = BigDecimal
                                    .valueOf(person.getIdealLoad())
                                    .multiply(BigDecimal.valueOf(PLANNING_MONTHS));

                            BigDecimal actual = BigDecimal.valueOf(shiftCount);
                            BigDecimal delta = actual.subtract(ideal).abs(); // |actual - ideal|
                            // square the deviation: delta²
                            return delta.pow(2);
                        }
                )
                .asConstraint("Ideal monthly load per person");
    }

    Constraint wellDistributedShifts(ConstraintFactory factory) {
        return factory.forEach(Person.class)
                .join(Shift.class,
                        Joiners.filtering((person, shift) ->
                                Objects.equals(person, shift.getPersonA())
                                        || Objects.equals(person, shift.getPersonB())
                        )
                )
                .groupBy(
                        (person, shift) -> person,
                        ConstraintCollectors.toList((person, shift) -> shift)
                )
                .penalizeBigDecimal(
                        HardSoftBigDecimalScore.ofSoft(new BigDecimal("0.3")),
                        this::calculateDistributionPenalty
                )
                .asConstraint("Shifts should be well distributed according to ideal load");
    }

    private BigDecimal calculateDistributionPenalty(Person person, List<Shift> shifts) {
        int n = shifts.size();
        if (n <= 1) {
            return BigDecimal.ZERO;
        }

        // Expected gap between this person's shifts, in days.
        // Example: idealLoad=1.0 => 28 days; idealLoad=2.0 => 14 days.
        double expectedGapDays = Util.getExpectedGapDays(person);

        // Sort shifts by date
        List<Shift> sorted = shifts.stream()
                .sorted(Comparator.comparing(Shift::getDate))
                .toList();

        double totalDeviationDays = 0.0;
        double minThreshold = 5; // Only punish if above 5 days
        double punishFactor = 1.25f; // Punish outliers harder

        for (int i = 1; i < sorted.size(); i++) {
            long gapDays = ChronoUnit.DAYS.between(
                    sorted.get(i - 1).getDate(),
                    sorted.get(i).getDate()
            );
            var currentDeviation = Math.max(Math.abs(gapDays - expectedGapDays) - minThreshold, 0);

            totalDeviationDays += Math.pow(currentDeviation, punishFactor);
        }

        // Convert to weeks to keep numbers reasonable
        BigDecimal totalDeviationWeeks = BigDecimal
                .valueOf(totalDeviationDays)
                .divide(BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP);

        return totalDeviationWeeks;
    }
}
