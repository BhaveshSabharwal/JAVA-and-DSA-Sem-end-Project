package com.example;

import java.util.List;
import java.util.Map;

public class TimetablePrinter {
    public void printTimetables(
            Map<String, Map<String, Map<String, Map<String, String>>>> timetables,
            List<String> weekDays,
            List<String> slotLabels,
            Map<String, Integer> durations) {

        for (Map.Entry<String, Map<String, Map<String, Map<String, String>>>> sectionEntry : timetables.entrySet()) {
            System.out.println("\n\n==== " + sectionEntry.getKey() + " Time Table ====");

            // Print header
            System.out.print(String.format("%-12s", "Day"));
            System.out.print(String.format("%-20s", "0900 - 1000"));
            System.out.print(String.format("%-20s", "1000 - 1100"));
            System.out.print(String.format("%-20s", "1100 - 1200"));
            System.out.print(String.format("%-20s", "1200 - 1300")); // Explicit recess slot
            System.out.print(String.format("%-20s", "1300 - 1400"));
            System.out.print(String.format("%-20s", "1400 - 1500"));
            System.out.print(String.format("%-20s", "1500 - 1600"));
            System.out.print(String.format("%-20s", "1600 - 1700"));
            System.out.println();

            System.out.println("-".repeat(12 + 20 * 8)); // Fixed width for 8 slots

            for (String day : weekDays) {
                System.out.print(String.format("%-12s", day));

                Map<String, Map<String, String>> daySchedule = sectionEntry.getValue().getOrDefault(day,
                        Map.of());

                // Morning slots
                for (int hour = 9; hour < 12; hour++) {
                    String time = String.format("%02d00", hour);
                    Map<String, String> entry = daySchedule.get(time);
                    printTimeSlot(entry, durations);
                }

                // Recess slot
                System.out.print(String.format("%-20s", "RECESS"));

                // Afternoon slots
                for (int hour = 13; hour < 17; hour++) {
                    String time = String.format("%02d00", hour);
                    Map<String, String> entry = daySchedule.get(time);
                    printTimeSlot(entry, durations);
                }
                System.out.println();
            }
        }
    }

    private void printTimeSlot(Map<String, String> entry, Map<String, Integer> durations) {
        if (entry != null) {
            String cell = entry.get("subject") + " (" + entry.get("teacher") + ")";
            if (durations.get(entry.get("subject")) == 2) {
                cell += " (2h)";
            }
            System.out.print(String.format("%-20s", cell));
        } else {
            System.out.print(String.format("%-20s", ""));
        }
    }
} 