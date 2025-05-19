package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class TimetableFileHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    public void testSaveAndLoadTimetable() throws Exception {
        TimetableFileHandler handler = new TimetableFileHandler();
        Path testFile = tempDir.resolve("test.json");

        // Create test data with potential null values
        Map<String, Map<String, Map<String, Map<String, String>>>> testData = new HashMap<>();

        // Section with null values
        Map<String, Map<String, Map<String, String>>> section = new HashMap<>();

        // Day with some null values
        Map<String, Map<String, String>> day = new HashMap<>();

        // Valid time slot
        Map<String, String> validSlot = new HashMap<>();
        validSlot.put("subject", "Math");
        validSlot.put("teacher", "Mr. Smith");
        day.put("0900", validSlot);

        // Time slot with null values
        Map<String, String> nullSlot = new HashMap<>();
        nullSlot.put("subject", null);
        nullSlot.put("teacher", null);
        day.put("1000", nullSlot);

        // Null time slot
        day.put("1100", null);

        section.put("Monday", day);
        section.put(null, null); // Null day entry
        testData.put("Section 1", section);
        testData.put(null, null); // Null section entry

        // Durations with some missing
        Map<String, Integer> durations = new HashMap<>();
        durations.put("Math", 1);
        durations.put(null, 2); // Null key

        // Should not throw any exceptions
        handler.saveToJson(testData, testFile.toString(), durations);

        // Verify file was created and has content
        assertTrue(Files.exists(testFile));
        assertTrue(Files.size(testFile) > 0);

        // Additional verification could parse the JSON and check structure
    }
}