package ch.kleemans.ludoplanung;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import ch.kleemans.ludoplanung.domain.LudoSchedule;
import ch.kleemans.ludoplanung.domain.Person;
import ch.kleemans.ludoplanung.domain.Shift;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
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
        Person alice = Person.builder().name("Alice").idealLoad(1.0f).availableDates(Set.of(date)).build();

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

        Person alice = Person.builder().name("Alice").idealLoad(1.0f).availableDates(Set.of(date1, date2)).build();
        Person bob = Person.builder().name("Bob").idealLoad(1.0f).availableDates(Set.of(date1, date2)).build();
        Person carl = Person.builder().name("Carl").idealLoad(1.0f).availableDates(Set.of(date2)).build();

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

        Person alice = Person.builder().name("Alice").idealLoad(1).availableDates(Set.of(date1, date2, date3)).build();
        Person bob = Person.builder().name("Bob").idealLoad(1).availableDates(Set.of(date1, date2, date3)).build();
        Person carl = Person.builder().name("Carl").idealLoad(1).availableDates(Set.of(date1, date2, date3)).build();
        Person doug = Person.builder().name("Doug").idealLoad(1).availableDates(Set.of(date1, date2, date3)).build();

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

        Person alice = Person.builder().name("Alice").idealLoad(1).availableDates(Set.of(allowed)).build(); // not available on forbidden
        Person bob = Person.builder().name("Bob").idealLoad(1).availableDates(Set.of(allowed, forbidden)).build(); // not available on forbidden
        Person carl = Person.builder().name("Carl").idealLoad(1).availableDates(Set.of(allowed, forbidden)).build(); // not available on forbidden

        Shift okShift = new Shift(allowed, bob, carl);               // OK
        Shift badShift = new Shift(forbidden, alice, carl);          // violates availability

        constraintVerifier.verifyThat(LudoConstraintProvider::personOnlyWhenAvailable)
                .given(okShift, badShift)
                .penalizesBy(1);
    }

    @Test
    void eachPersonPlannedAtLeastOnce_missingCarl() {
        LocalDate date1 = LocalDate.of(2026, 2, 10);
        LocalDate date2 = LocalDate.of(2026, 2, 13);

        Person alice = Person.builder().name("Alice").idealLoad(1).availableDates(Set.of(date1)).build();
        Person bob = Person.builder().name("Bob").idealLoad(1).availableDates(Set.of(date1, date2)).build();
        Person carl = Person.builder().name("Carl").idealLoad(1).availableDates(Set.of(date1, date2)).build();

        Shift okShift = new Shift(date1, alice, bob);
        Shift badShift = new Shift(date2, alice, bob);

        constraintVerifier.verifyThat(LudoConstraintProvider::eachPersonPlannedAtLeastOnce)
                .given(alice, bob, carl, okShift, badShift)
                .penalizesBy(1);
    }

    @Test
    void avoidUnwantedDate_aliceWorksOnFridayAlthoughUnwanted() {
        LocalDate date1 = LocalDate.of(2026, 2, 10); // Tuesday
        LocalDate date2 = LocalDate.of(2026, 2, 13); // Friday

        Person alice = Person.builder().name("Alice").idealLoad(1).availableDates(Set.of(date1, date2)).unwantedDaysOfWeek(Set.of(DayOfWeek.FRIDAY)).build();
        Person bob = Person.builder().name("Bob").idealLoad(1).availableDates(Set.of(date1, date2)).build();
        Person carl = Person.builder().name("Carl").idealLoad(1).availableDates(Set.of(date1, date2)).build();

        Shift okShift = new Shift(date1, alice, bob);  //  OK
        Shift unwantedShift = new Shift(date2, alice, carl); // Friday => Alice doesn't like to work then

        constraintVerifier.verifyThat(LudoConstraintProvider::avoidUnwantedDates)
                .given(alice, bob, carl, okShift, unwantedShift)
                .penalizesBy(1);
    }

    @Test
    void idealMonthlyLoad_alice2Away() {
        Person alice = Person.builder().name("Alice").idealLoad(1.0f).build(); // ideal 5 shifts total
        Person bob = Person.builder().name("Bob").idealLoad(1.0f).build(); // ideal 5 shifts total

        // 7 shifts for Alice in 5-month planning
        List<Shift> shifts = IntStream.rangeClosed(1, 7)
                .mapToObj(i -> new Shift("01.0" + i + ".26", alice, bob))
                .toList();

        constraintVerifier.verifyThat(LudoConstraintProvider::idealMonthlyLoad)
                .given(Stream.concat(Stream.of(alice), shifts.stream()).toArray())
                .penalizesBy(4); // |7 - 5|^2 = 4
    }

    @Test
    void idealMonthlyLoad_aliceAndBobEach2Away() {
        Person alice = Person.builder().name("Alice").idealLoad(1.0f).build(); // ideal 5 shifts total
        Person bob = Person.builder().name("Bob").idealLoad(1.0f).build(); // ideal 5 shifts total

        // 6 shifts for both in 5-month planning
        List<Shift> shifts = IntStream.rangeClosed(1, 6)
                .mapToObj(i -> new Shift("01.0" + i + ".26", alice, bob))
                .toList();

        constraintVerifier.verifyThat(LudoConstraintProvider::idealMonthlyLoad)
                .given(Stream.concat(Stream.of(alice, bob), shifts.stream()).toArray())
                .penalizesBy(2); // |6 - 5| * 2 = 2
    }
}
