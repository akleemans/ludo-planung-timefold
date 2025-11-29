package ch.kleemans.ludoplanung;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import ch.kleemans.ludoplanung.domain.LudoSchedule;
import ch.kleemans.ludoplanung.domain.Person;
import ch.kleemans.ludoplanung.domain.Shift;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class LudoConstraintProviderTest {

    private final ConstraintVerifier<LudoConstraintProvider, LudoSchedule> constraintVerifier =
            ConstraintVerifier.build(new LudoConstraintProvider(), LudoSchedule.class, Shift.class);

    @Test
    void noSamePersonTwiceInShift() {
        LocalDate date = LocalDate.of(2026, 2, 10);
        Person alice = new Person("Alice", 1f, Set.of(date));

        Shift badShift = new Shift(date, alice, alice);

        constraintVerifier.verifyThat(LudoConstraintProvider::noSamePersonTwiceInShift)
                .given(badShift)
                .penalizesBy(1);
    }

    @Test
    void noPersonTwiceInSameWeek_aliceTwice() {
        // Same ISO week
        LocalDate date1 = LocalDate.of(2026, 2, 10);
        LocalDate date2 = LocalDate.of(2026, 2, 13);

        Person alice = new Person("Alice", 1, Set.of(date1, date2));
        Person bob = new Person("Bob", 1, Set.of(date1, date2));
        Person carl = new Person("Carl", 1, Set.of(date2));

        // Alice again in same week
        Shift shift1 = new Shift(date1, alice, bob);
        Shift shift2 = new Shift(date2, alice, carl);

        constraintVerifier.verifyThat(LudoConstraintProvider::noPersonTwiceInSameWeek)
                .given(shift1, shift2)
                .penalizesBy(1);
    }

    @Test
    void noPersonTwiceInSameWeek_twoPeopleTwice() {
        // Same ISO week
        LocalDate date1 = LocalDate.of(2026, 2, 10);
        LocalDate date2 = LocalDate.of(2026, 2, 13);
        LocalDate date3 = LocalDate.of(2026, 2, 14);

        Person alice = new Person("Alice", 1, Set.of(date1, date2, date3));
        Person bob = new Person("Bob", 1, Set.of(date1, date2, date3));
        Person carl = new Person("Carl", 1, Set.of(date1, date2, date3));
        Person doug = new Person("Doug", 1, Set.of(date1, date2, date3));

        // Alice again in same week
        Shift shift1 = new Shift(date1, alice, bob);
        Shift shift2 = new Shift(date2, alice, carl);
        Shift shift3 = new Shift(date2, bob, doug);

        constraintVerifier.verifyThat(LudoConstraintProvider::noPersonTwiceInSameWeek)
                .given(shift1, shift2, shift3)
                .penalizesBy(2);
    }

    @Test
    void personOnlyWhenAvailable() {
        LocalDate allowed = LocalDate.of(2026, 2, 10);
        LocalDate forbidden = LocalDate.of(2026, 2, 13);

        Person alice = new Person("Alice", 1, Set.of(allowed));               // not available on forbidden
        Person bob = new Person("Bob", 1, Set.of(allowed, forbidden));    // always available
        Person carl = new Person("Bob", 1, Set.of(allowed, forbidden));

        Shift okShift = new Shift(allowed, bob, carl);               // OK
        Shift badShift = new Shift(forbidden, alice, carl);          // violates availability

        constraintVerifier.verifyThat(LudoConstraintProvider::personOnlyWhenAvailable)
                .given(okShift, badShift)
                .penalizesBy(1);
    }

    @Test
    void idealMonthlyLoad_penalizesDeviationFromIdeal() {
        Person alice = new Person("Alice", 1.0f, Set.of()); // ideal 5 shifts total
        Person bob = new Person("Bob", 1.0f, Set.of()); // ideal 5 shifts total

        // 7 shifts for Alice in 5-month planning
        List<Shift> shifts = IntStream.rangeClosed(1, 7)
                .mapToObj(i -> new Shift("01.0" + i + ".26", alice, bob))
                .toList();

        constraintVerifier.verifyThat(LudoConstraintProvider::idealMonthlyLoad)
                .given(Stream.concat(Stream.of(alice), shifts.stream()).toArray())
                .penalizesBy(2); // |7 - 5| = 2
    }
}
