package com.example;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class TimetableGeneratorTest {

    @Test
    void testTimetableGeneration() {
        TimetableGenerator generator = new TimetableGenerator();
        List<String> subjects = List.of("Math", "Physics");
        Map<String, Integer> durations = Map.of("Math", 1, "Physics", 2);
        Map<String, Integer> sessions = Map.of("Math", 3, "Physics", 2);
        Map<String, List<String>> teachers = Map.of(
                "Math", List.of("Mr. A"),
                "Physics", List.of("Ms. B"));
        List<String> days = List.of("Monday", "Tuesday");

        var timetable = generator.generateTimetable(
                subjects, durations, sessions, teachers, days, 2);

        assertNotNull(timetable);
        assertEquals(2, timetable.size()); // 2 sections
    }
}