package ch.kleemans.ludoplanung.domain;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UtilTest {

    @Test
    void getWeekNumber() {
        assertThat(Util.getWeekNumber(LocalDate.of(2026, 1, 1))).isEqualTo(1);
        assertThat(Util.getWeekNumber(LocalDate.of(2026, 1, 7))).isEqualTo(2);
        assertThat(Util.getWeekNumber(LocalDate.of(2026, 1, 11))).isEqualTo(2);
        assertThat(Util.getWeekNumber(LocalDate.of(2026, 2, 10))).isEqualTo(7);
        assertThat(Util.getWeekNumber(LocalDate.of(2026, 12, 31))).isEqualTo(53);
    }

    @Test
    void getDayOfWeek() {
        assertThat(Util.getDayOfWeek(LocalDate.of(2026, 1, 1))).isEqualTo(DayOfWeek.THURSDAY);
        assertThat(Util.getDayOfWeek(LocalDate.of(2026, 1, 7))).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(Util.getDayOfWeek(LocalDate.of(2026, 1, 8))).isEqualTo(DayOfWeek.THURSDAY);
        assertThat(Util.getDayOfWeek(LocalDate.of(2026, 2, 10))).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(Util.getDayOfWeek(LocalDate.of(2026, 12, 31))).isEqualTo(DayOfWeek.THURSDAY);
    }

    @Test
    void getDayOfWeek_str() {
        assertThat(Util.getDayOfWeek("Mo")).isEqualTo(DayOfWeek.MONDAY);
        assertThat(Util.getDayOfWeek("Di")).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(Util.getDayOfWeek("Mi")).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(Util.getDayOfWeek("Do")).isEqualTo(DayOfWeek.THURSDAY);
        assertThat(Util.getDayOfWeek("Fr")).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(Util.getDayOfWeek("Sa")).isEqualTo(DayOfWeek.SATURDAY);
        assertThat(Util.getDayOfWeek("So")).isEqualTo(DayOfWeek.SUNDAY);
        assertThat(Util.getDayOfWeek("gibberish")).isEqualTo(DayOfWeek.SUNDAY);
    }

}
