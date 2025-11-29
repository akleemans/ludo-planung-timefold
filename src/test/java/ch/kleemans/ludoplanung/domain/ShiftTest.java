package ch.kleemans.ludoplanung.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftTest {
    private final Shift shift1 = new Shift("10.02.26");
    private final Shift shift2 = new Shift("13.02.26");
    private final Shift shift3 = new Shift("14.02.26");
    private final Shift shift4 = new Shift("17.02.26");

    @Test
    void getWeekNumber() {
        assertThat(shift1.getWeekNumber()).isEqualTo(7);
        assertThat(shift1.getWeekNumber()).isEqualTo(shift2.getWeekNumber());
        assertThat(shift1.getWeekNumber()).isEqualTo(shift3.getWeekNumber());

        assertThat(shift1.getWeekNumber()).isNotEqualTo(shift4.getWeekNumber());
    }
}
