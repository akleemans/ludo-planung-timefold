package ch.kleemans.ludoplanung.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;

public class Util {

    public static int getWeekNumber(LocalDate date) {
        return date.get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    public static DayOfWeek getDayOfWeek(LocalDate date) {
        return date.getDayOfWeek();
    }

    public static DayOfWeek getDayOfWeek(String shortString) {
        if (shortString.equalsIgnoreCase("MO")) {
            return DayOfWeek.MONDAY;
        } else if (shortString.equalsIgnoreCase("DI")) {
            return DayOfWeek.TUESDAY;
        } else if (shortString.equalsIgnoreCase("MI")) {
            return DayOfWeek.WEDNESDAY;
        } else if (shortString.equalsIgnoreCase("DO")) {
            return DayOfWeek.THURSDAY;
        } else if (shortString.equalsIgnoreCase("FR")) {
            return DayOfWeek.FRIDAY;
        } else if (shortString.equalsIgnoreCase("SA")) {
            return DayOfWeek.SATURDAY;
        } else {
            return DayOfWeek.SUNDAY;
        }
    }
}
