package com.example;

import java.util.*;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import static org.junit.jupiter.api.Assertions.*;

public class UserInputHandlerTest {

    @Test
    void testGetSubjects() {
        String input = "Math,Science";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        UserInputHandler handler = new UserInputHandler();
        List<String> subjects = handler.getSubjects();

        assertEquals(List.of("Math", "Science"), subjects);
    }

    @Test
    void testGetDurations() {
        String input = "1\n2\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        UserInputHandler handler = new UserInputHandler();
        Map<String, Integer> durations = handler.getDurations(List.of("Math", "Science"));

        assertEquals(1, durations.get("Math"));
        assertEquals(2, durations.get("Science"));
    }
}