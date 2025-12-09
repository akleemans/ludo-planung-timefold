package ch.kleemans.ludoplanung;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ch.kleemans.ludoplanung.domain.LudoSchedule;
import ch.kleemans.ludoplanung.domain.Person;
import ch.kleemans.ludoplanung.domain.Shift;
import ch.kleemans.ludoplanung.domain.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class LudoApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(LudoApp.class);

    public static void main(String[] args) {
        SolverFactory<LudoSchedule> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(LudoSchedule.class)
                .withEntityClasses(Shift.class)
                .withConstraintProviderClass(LudoConstraintProvider.class)
                // It's recommended to run for at least 5 minutes
                .withTerminationSpentLimit(Duration.ofSeconds(5)));

        // Load the problem
        LudoSchedule problem = loadData();
        System.out.println("Loaded people:" + problem.getPeople());
        System.out.println("Loaded shifts:" + problem.getShifts());

        for (var shift : problem.getShifts()) {
            var availablePeople = "";
            for (var person : problem.getPeople()) {
                if (person.isAvailable(shift.getDate())) {
                    availablePeople += person.getName() + ", ";
                }
            }
            System.out.println(shift + ": " + availablePeople);

        }

        // Solve the problem
        Solver<LudoSchedule> solver = solverFactory.buildSolver();
        LudoSchedule solution = solver.solve(problem);

        System.out.println(solution);
    }

    private static List<String> fileToLines(String fileName) {
        List<String> lines;
        try {
            Path path = Paths.get(LudoApp.class.getClassLoader().getResource(fileName).toURI());
            lines = Files.readAllLines(path);
        } catch (Exception e) {
            throw new RuntimeException("File not found! " + fileName);
        }
        lines.remove(0);
        return lines;
    }

    public static LudoSchedule loadData() {
        // Load Shifts
        List<Shift> shifts = new ArrayList<>();
        for (String line : fileToLines("daten_ludo.txt")) {
            shifts.add(new Shift(line.split("\t")[0]));
        }

        List<Person> people = new ArrayList<>();
        var nameCol = 1;
        var idealLoadCol = 2;
        var unwantedCol = 3;
        var datesCol = 4;
        for (String line : fileToLines("form_answers.csv")) {
            var attributes = line.split(",");
            var name = attributes[nameCol].replace("\"", "");
            float idealLoad = Float.parseFloat(attributes[idealLoadCol]);
            String unwantedDaysStr = attributes[unwantedCol];
            Set<DayOfWeek> unwantedDays = Set.of();
            if (!unwantedDaysStr.isEmpty()) {
                unwantedDays = Arrays.stream(unwantedDaysStr.split(";")).map(Util::getDayOfWeek).collect(Collectors.toSet());
            }

            var dateStrings = attributes[datesCol].replace("\"", "").split(";");
            Set<LocalDate> dates = new HashSet<>();
            for (var dateString : dateStrings) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
                var date = LocalDate.parse(dateString.split(" ")[0], formatter);
                dates.add(date);
            }
            System.out.println(name + " has available dates: " + dates);
            people.add(Person.builder().name(name).idealLoad(idealLoad).availableDates(dates).unwantedDaysOfWeek(unwantedDays).build());
        }

        // Validation
        var datesFromShifts = shifts.stream().map(Shift::getDate).collect(Collectors.toSet());
        var datesFromPeople = people.stream().map(Person::getAvailableDates).flatMap(Collection::stream).collect(Collectors.toSet());

        if (!datesFromShifts.equals(datesFromPeople)) {
            Set<LocalDate> onlyInShifts = new HashSet<>(datesFromShifts);
            onlyInShifts.removeAll(datesFromPeople);

            Set<LocalDate> onlyInPeople = new HashSet<>(datesFromPeople);
            onlyInPeople.removeAll(datesFromShifts);
            throw new RuntimeException("Date mismatch! onlyInShifts:" + onlyInShifts + ", onlyInPeople:" + onlyInPeople);
        }

        return new LudoSchedule(shifts, people);
    }
}
