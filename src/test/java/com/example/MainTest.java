package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MainTest {

    @Test
    void testGenerateDailySlots() {
        List<Integer> slots = Main.generateDailySlots();
        // Should exclude recess (12-13) and end at 17:00
        assertEquals(List.of(9, 10, 11, 13, 14, 15, 16), slots);
    }

    @Test
    void testGenerateSlotLabels() {
        List<String> labels = Main.generateSlotLabels(List.of(9, 10));
        assertEquals("0900 - 1000", labels.get(0));
        assertEquals("1000 - 1100", labels.get(1));
    }

    @Test
    void testAssignSubjectsWithTeachers() {
        List<String> subjects = List.of("Math");
        Map<String, Integer> durations = Map.of("Math", 1);
        Map<String, Integer> sessions = Map.of("Math", 2);
        Map<String, List<String>> teachers = Map.of("Math", List.of("Mr. Smith"));
        List<String> days = List.of("Monday");

        Map<String, Map<String, Map<String, Map<String, String>>>> timetable = Main.assignSubjectsWithTeachers(
                subjects, durations, sessions, teachers, days, 1, List.of(9, 10));

        assertNotNull(timetable);
        assertEquals(1, timetable.size()); // 1 section
        assertTrue(timetable.containsKey("Section 1"));
    }
}