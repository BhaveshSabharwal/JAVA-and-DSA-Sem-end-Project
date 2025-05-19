package com.example;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.example.detastructures.ArrayList;
import com.example.detastructures.HashMap;
import com.example.detastructures.HashSet;
import com.example.detastructures.Queue;

public class Main {
    // Constants for timetable configuration
    public static final int START_TIME = 9; // School day starts at 9:00
    public static final int END_TIME = 17; // School day ends at 17:00
    public static final int RECESS_START = 12; // Recess starts at 12:00
    public static final int RECESS_END = 13; // Recess ends at 13:00

    // Utility objects
    public static final Random random = new Random(); // For random selections
    public static final Scanner scanner = new Scanner(System.in); // For user input

    public static void main(String[] args) {
        // Display welcome message
        System.out.println("🎓 Student Timetable Generator 🎓");
        System.out.println("=================================");

        // Initialize helper classes
        UserInputHandler inputHandler = new UserInputHandler();
        TimetableGenerator timetableGenerator = new TimetableGenerator();
        TimetablePrinter timetablePrinter = new TimetablePrinter();
        TimetableFileHandler fileHandler = new TimetableFileHandler();
        deleteTimetable deleteTimetable = new deleteTimetable();

        // Main program loop
        while (true) {
            // Get user's menu choice
            int choice = inputHandler.getMenuChoice();

            try {
                // Process user choice
                switch (choice) {
                    case 1 -> {
                        // Generate timetable interactively with user input
                        generateTimetableInteractive(inputHandler, timetableGenerator, timetablePrinter,
                                fileHandler);
                    }
                    case 2 -> {
                        // Generate timetable from input file
                        generateTimetableFromFile(inputHandler, timetableGenerator, timetablePrinter, fileHandler);
                    }
                    case 3 -> {
                        // Print existing timetable from file
                        printExistingTimetable(inputHandler, timetablePrinter, fileHandler);
                    }
                    case 4 -> {
                        // Generate or edit input configuration file
                        inputHandler.generateOrEditInputFile();
                    }
                    case 5 -> {
                        // Delete timetable file
                        deleteTimetableFile(inputHandler, fileHandler);
                    }
                    case 6 -> {
                        // Exit program
                        System.out.println("Exiting program. Goodbye!");
                        return;
                    }
                    default -> {
                        System.out.println("Invalid choice. Please try again.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Generates a timetable interactively with user input and validation
     */
    public static void generateTimetableInteractive(
            UserInputHandler inputHandler,
            TimetableGenerator timetableGenerator,
            TimetablePrinter timetablePrinter,
            TimetableFileHandler fileHandler) throws Exception {

        // Get all required inputs from user with validation
        List<String> subjects = inputHandler.getSubjects();
        List<String> weekDays = inputHandler.getWeekDays();
        Map<String, Integer> durations = inputHandler.getDurations(subjects);
        Map<String, Integer> sessionsPerWeek = getSessionsPerWeek(subjects, weekDays);
        Map<String, List<String>> teachers = getTeachers(subjects);

        // Validate inputs before generation
        validateSessionsPerWeek(sessionsPerWeek, weekDays);
        validateTeachers(teachers, subjects);

        // Generate time slots for the timetable
        List<Integer> dailySlots = timetableGenerator.generateDailySlots();
        List<String> slotLabels = timetableGenerator.generateSlotLabels(dailySlots);

        // Generate the actual timetable
        Map<String, Map<String, Map<String, Map<String, String>>>> timetables = timetableGenerator
                .generateTimetable(
                        subjects, durations, sessionsPerWeek, teachers, weekDays,
                        inputHandler.getNumberOfSections());

        // Print the generated timetable
        timetablePrinter.printTimetables(timetables, weekDays, slotLabels, durations);

        // Save to file
        String filename = inputHandler.getFilename("Enter filename to save (e.g., timetables.json): ");
        fileHandler.saveToJson(timetables, filename, durations);

        System.out.println("\n✅ Timetable generation complete!");
    }

    /**
     * Validates if sessions per week exceed available days
     * 
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateSessionsPerWeek(
            Map<String, Integer> sessionsPerWeek,
            List<String> weekDays) {
        int maxSessions = weekDays.size() * 2; // Max 2 sessions/day per subject
        for (Map.Entry<String, Integer> entry : sessionsPerWeek.entrySet()) {
            if (entry.getValue() > maxSessions) {
                throw new IllegalArgumentException(
                        "❌ Too many sessions for " + entry.getKey() +
                                ". Max allowed: " + maxSessions + " (for " +
                                weekDays.size() + " days)");
            }
        }
    }

    /**
     * Validates that all subjects have at least one teacher assigned
     * 
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateTeachers(
            Map<String, List<String>> teachers,
            List<String> subjects) {
        for (String subject : subjects) {
            if (!teachers.containsKey(subject)) {
                throw new IllegalArgumentException(
                        "❌ No teachers assigned for subject: " + subject);
            }
            if (teachers.get(subject).isEmpty()) {
                throw new IllegalArgumentException(
                        "❌ Empty teacher list for subject: " + subject);
            }
        }
    }

    /**
     * Generates a timetable from an input configuration file
     */
    public static void generateTimetableFromFile(
            UserInputHandler inputHandler,
            TimetableGenerator timetableGenerator,
            TimetablePrinter timetablePrinter,
            TimetableFileHandler fileHandler) throws Exception {

        // Load input configuration from file
        String inputFile = inputHandler.getFilename("Enter input JSON filename (e.g., input.json): ");
        Map<String, Object> inputData = fileHandler.loadInputFile(inputFile);

        // Extract configuration data from input file
        @SuppressWarnings("unchecked")
        List<String> subjects = (List<String>) inputData.get("subjects");
        @SuppressWarnings("unchecked")
        Map<String, Integer> durations = (Map<String, Integer>) inputData.get("durations");
        @SuppressWarnings("unchecked")
        Map<String, Integer> sessionsPerWeek = (Map<String, Integer>) inputData.get("sessionsPerWeek");
        @SuppressWarnings("unchecked")
        Map<String, List<String>> teachers = (Map<String, List<String>>) inputData.get("teachers");
        @SuppressWarnings("unchecked")
        List<String> weekDays = (List<String>) inputData.get("weekDays");
        int numSections = (int) inputData.get("numSections");

        // Generate time slots
        List<Integer> dailySlots = timetableGenerator.generateDailySlots();
        List<String> slotLabels = timetableGenerator.generateSlotLabels(dailySlots);

        // Generate timetable
        Map<String, Map<String, Map<String, Map<String, String>>>> timetables = timetableGenerator
                .generateTimetable(
                        subjects, durations, sessionsPerWeek, teachers, weekDays, numSections);

        // Print and save timetable
        timetablePrinter.printTimetables(timetables, weekDays, slotLabels, durations);
        String filename = inputHandler.getFilename("Enter filename to save (e.g., timetables.json): ");
        fileHandler.saveToJson(timetables, filename, durations);

        System.out.println("\n✅ Timetable generation complete!");
    }

    /**
     * Loads and prints an existing timetable from a file
     */
    public static void printExistingTimetable(
            UserInputHandler inputHandler,
            TimetablePrinter timetablePrinter,
            TimetableFileHandler fileHandler) throws Exception {

        // Load timetable file
        String filename = inputHandler
                .getFilename("Enter timetable JSON filename to print (e.g., timetable.json): ");
        Map<String, Map<String, Map<String, Map<String, String>>>> timetables = fileHandler.loadTimetable(filename);

        // Extract week days from the timetable data
        List<String> weekDays = timetables.values().stream()
                .flatMap(section -> section.keySet().stream())
                .distinct()
                .sorted()
                .toList();

        // Generate time slot labels
        List<Integer> dailySlots = List.of(9, 10, 11, 13, 14, 15, 16);
        List<String> slotLabels = dailySlots.stream()
                .map(t -> String.format("%02d00 - %02d00", t, t + 1))
                .toList();

        // Extract subject durations from the timetable
        Map<String, Integer> durations = new java.util.HashMap<>();
        timetables.values().forEach(section -> section.values().forEach(day -> day.values().forEach(time -> {
            String subject = time.get("subject");
            if (!durations.containsKey(subject)) {
                durations.put(subject, 1); // Default duration
            }
        })));

        // Print the timetable
        timetablePrinter.printTimetables(timetables, weekDays, slotLabels, durations);
    }

    /**
     * Generates or edits an input configuration file
     */
    public static void generateOrEditInputFile() {
        System.out.println("\n📝 Generate/Edit Input JSON File");
        System.out.println("===============================");

        // Try to load existing file if it exists
        JSONObject inputJson = new JSONObject();
        System.out.print("Enter filename to load/edit (or leave blank to create new): ");
        String filename = scanner.nextLine();

        if (!filename.isEmpty()) {
            try {
                JSONParser parser = new JSONParser();
                inputJson = (JSONObject) parser.parse(new FileReader(filename));
                System.out.println("Existing file loaded successfully.");
            } catch (Exception e) {
                System.out.println("File not found or invalid. Creating new configuration.");
            }
        }

        // Get basic configuration
        System.out.print("Number of days per week (default 5): ");
        String daysInput = scanner.nextLine();
        int daysPerWeek = daysInput.isEmpty() ? 5 : Integer.parseInt(daysInput);
        inputJson.put("days_per_week", daysPerWeek);

        System.out.print("Number of sections (default 3): ");
        String sectionsInput = scanner.nextLine();
        int sections = sectionsInput.isEmpty() ? 3 : Integer.parseInt(sectionsInput);
        inputJson.put("sections", sections);

        // Subject configuration
        JSONArray subjectsArray = new JSONArray();
        if (inputJson.containsKey("subjects")) {
            System.out.println("\nCurrent subjects:");
            JSONArray existingSubjects = (JSONArray) inputJson.get("subjects");
            for (Object subjObj : existingSubjects) {
                JSONObject subject = (JSONObject) subjObj;
                System.out.println("- " + subject.get("name") +
                        " (Duration: " + subject.get("duration") + "h, " +
                        "Sessions: " + subject.get("sessions") + ", " +
                        "Teachers: " + subject.get("teachers") + ")");
            }
            System.out.print("\nKeep existing subjects? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                subjectsArray = existingSubjects;
            }
        }

        // Add new subjects if needed
        if (subjectsArray.isEmpty()) {
            System.out.println("\nEnter subjects (leave name blank to finish):");
            while (true) {
                System.out.print("Subject name: ");
                String name = scanner.nextLine();
                if (name.isEmpty())
                    break;

                JSONObject subject = new JSONObject();
                subject.put("name", name);

                System.out.print("Duration (1 or 2 hours): ");
                int duration = scanner.nextInt();
                scanner.nextLine(); // consume newline
                subject.put("duration", duration);

                System.out.print("Sessions per week: ");
                int sessions = scanner.nextInt();
                scanner.nextLine(); // consume newline
                subject.put("sessions", sessions);

                System.out.print("Teachers (comma separated): ");
                String[] teachers = scanner.nextLine().split(",");
                JSONArray teachersArray = new JSONArray();
                for (String teacher : teachers) {
                    teachersArray.add(teacher.trim());
                }
                subject.put("teachers", teachersArray);

                subjectsArray.add(subject);
            }
        }
        inputJson.put("subjects", subjectsArray);

        // Save to file
        System.out.print("\nEnter filename to save: ");
        String saveFilename = scanner.nextLine();
        try (FileWriter file = new FileWriter(saveFilename)) {
            file.write(inputJson.toJSONString());
            System.out.println("✅ Input file saved to " + saveFilename);
        } catch (IOException e) {
            System.err.println("❌ Error saving file: " + e.getMessage());
        }
    }

    // Delete existing Timetable
    public static void deleteTimetableFile(UserInputHandler inputHandler,
            TimetableFileHandler fileHandler) {

        System.out.println("\n🗑️ Delete Timetable File");
        System.out.println("=======================");

        String filename = inputHandler.getFilename("Enter filename to delete (e.g., timetable.json): ");

        if (new deleteTimetable().deleteFile(filename)) {
            System.out.println("File deletion successful");
        } else {
            System.out.println("File deletion failed");
        }
    }

    public static List<String> getSubjects() {
        System.out.println("\nEnter subjects (comma separated, e.g.: Math,English,Biology):");
        String input = scanner.nextLine();

        while (input.trim().isEmpty()) {
            System.out.println("❌ At least one subject is required!");
            input = scanner.nextLine();
        }

        String[] subjectArray = input.split(",");
        List<String> subjects = new ArrayList<>();
        for (String subject : subjectArray) {
            subjects.add(subject.trim());
        }
        return subjects;
    }

    public static Map<String, Integer> getDurations(List<String> subjects) {
        Map<String, Integer> durations = new HashMap<>();
        System.out.println("\nEnter duration (in hours) for each subject (1 or 2 only):");

        for (String subject : subjects) {
            int duration;
            do {
                System.out.print(subject + ": ");
                duration = scanner.nextInt();
                if (duration != 1 && duration != 2) {
                    System.out.println("❌ Please enter only 1 or 2 for duration");
                }
            } while (duration != 1 && duration != 2); // Keeps asking until valid
            durations.put(subject, duration);
            scanner.nextLine(); // Consume newline
        }
        return durations;
    }

    /**
     * Gets number of sessions per week for each subject with validation
     */
    public static Map<String, Integer> getSessionsPerWeek(
            List<String> subjects,
            List<String> weekDays) {
        Map<String, Integer> sessions = new HashMap<>();
        int maxSessions = weekDays.size() * 2;

        System.out.println("\nEnter number of sessions per week for each subject");
        System.out.println("(Maximum " + maxSessions + " sessions allowed):");

        for (String subject : subjects) {
            while (true) {
                try {
                    System.out.print(subject + ": ");
                    int numSessions = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    if (numSessions <= 0) {
                        System.out.println("❌ Sessions must be greater than 0");
                        continue;
                    }
                    if (numSessions > maxSessions) {
                        System.out.println("❌ Cannot exceed " + maxSessions + " sessions");
                        continue;
                    }

                    sessions.put(subject, numSessions);
                    break;
                } catch (InputMismatchException e) {
                    System.out.println("❌ Please enter a valid number");
                    scanner.nextLine(); // Clear invalid input
                }
            }
        }
        return sessions;
    }

    /**
     * Gets teachers for each subject with validation
     */
    public static Map<String, List<String>> getTeachers(List<String> subjects) {
        Map<String, List<String>> teachers = new HashMap<>();
        System.out.println("\nEnter at least one teacher per subject (comma separated):");

        for (String subject : subjects) {
            while (true) {
                System.out.print(subject + ": ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("❌ At least one teacher required");
                    continue;
                }

                String[] teacherArray = input.split(",");
                List<String> teacherList = new ArrayList<>();
                for (String teacher : teacherArray) {
                    String trimmed = teacher.trim();
                    if (!trimmed.isEmpty()) {
                        teacherList.add(trimmed);
                    }
                }

                if (teacherList.isEmpty()) {
                    System.out.println("❌ Invalid teacher list");
                } else {
                    teachers.put(subject, teacherList);
                    break;
                }
            }
        }
        return teachers;
    }

    public static List<String> getWeekDays() {
        System.out
                .println("\nEnter week days (comma separated, default: Monday,Tuesday,Wednesday,Thursday,Friday):");
        String input = scanner.nextLine();
        if (input.trim().isEmpty()) {
            return List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        }
        String[] dayArray = input.split(",");
        List<String> days = new ArrayList<>();
        for (String day : dayArray) {
            days.add(day.trim());
        }
        return days;
    }

    /**
     * Generates time slots for the school day (9:00-17:00, excluding recess)
     */
    public static List<Integer> generateDailySlots() {
        List<Integer> slots = new ArrayList<>();
        int t = START_TIME;
        while (t < END_TIME) {
            if (t == RECESS_START) { // Skip the recess hour completely
                t = RECESS_END;
                continue;
            }
            slots.add(t);
            t++;
        }
        return slots;
    }

    /**
     * Creates formatted time slot labels (e.g., "0900 - 1000")
     */
    public static List<String> generateSlotLabels(List<Integer> slots) {
        List<String> labels = new ArrayList<>();
        for (int t : slots) {
            labels.add(String.format("%02d00 - %02d00", t, t + 1));
        }
        return labels;
    }

    public static Map<String, Map<String, Map<String, Map<String, String>>>> assignSubjectsWithTeachers(
            List<String> subjects, Map<String, Integer> durations, Map<String, Integer> sessionsPerWeek,
            Map<String, List<String>> teachers, List<String> weekDays, int numSections, List<Integer> dailySlots) {

        Map<String, Map<String, Map<String, Map<String, String>>>> sectionTimetable = new HashMap<>();

        for (int sec = 1; sec <= numSections; sec++) {
            String sectionName = "Section " + sec;
            Map<String, Map<String, Map<String, String>>> dayMap = new HashMap<>();
            Set<String> usedSlots = new HashSet<>();
            Map<String, Map<String, Integer>> subjectDayCounts = new HashMap<>();
            Queue<SubjectSession> sessionQueue = new Queue<>();

            // Initialize subjectDayCounts
            for (String subject : subjects) {
                Map<String, Integer> dayCount = new HashMap<>();
                for (String day : weekDays) {
                    dayCount.put(day, 0);
                }
                subjectDayCounts.put(subject, dayCount);
            }

            // Create subject sessions and add them to the queue
            for (String subj : subjects) {
                int sessions = sessionsPerWeek.get(subj);
                for (int i = 0; i < sessions; i++) {
                    String teacher = teachers.get(subj).get(random.nextInt(teachers.get(subj).size()));
                    sessionQueue.add(new SubjectSession(subj, durations.get(subj), teacher));
                }
            }

            int maxAttempts = sessionQueue.size() * 100;
            int attempt = 0;

            while (!sessionQueue.isEmpty() && attempt < maxAttempts) {
                SubjectSession session = sessionQueue.poll();
                boolean placed = false;

                Collections.shuffle(weekDays); // Randomize day preference

                for (String day : weekDays) {
                    int currentCount = subjectDayCounts.get(session.subject).get(day);
                    if ((session.duration == 1 && currentCount >= 2) || (session.duration == 2 && currentCount >= 1)) {
                        continue;
                    }

                    int startIdx = -1;
                    List<Integer> slotRange = new ArrayList<>();

                    if (session.duration == 2) {
                        for (int i = 0; i < dailySlots.size() - 1; i++) {
                            if (i >= dailySlots.size() - 2)
                                continue;
                            int first = dailySlots.get(i), second = dailySlots.get(i + 1);
                            if (first + 1 == second) {
                                String t1 = day + "-" + String.format("%02d00", first);
                                String t2 = day + "-" + String.format("%02d00", second);
                                if (!usedSlots.contains(t1) && !usedSlots.contains(t2)) {
                                    slotRange = Arrays.asList(first, second);
                                    startIdx = i;
                                    break;
                                }
                            }
                        }
                    } else {
                        for (int i = 0; i < dailySlots.size(); i++) {
                            int slot = dailySlots.get(i);
                            String timeKey = day + "-" + String.format("%02d00", slot);
                            if (!usedSlots.contains(timeKey)) {
                                slotRange = List.of(slot);
                                startIdx = i;
                                break;
                            }
                        }
                    }

                    if (startIdx == -1 || slotRange.isEmpty())
                        continue;

                    // Final check for conflicts
                    boolean hasConflict = false;
                    for (int slot : slotRange) {
                        String timeKey = day + "-" + String.format("%02d00", slot);
                        if (usedSlots.contains(timeKey)) {
                            hasConflict = true;
                            break;
                        }
                    }

                    if (hasConflict)
                        continue;

                    // Assign session
                    for (int slot : slotRange) {
                        String timeKey = day + "-" + String.format("%02d00", slot);
                        usedSlots.add(timeKey);

                        dayMap.putIfAbsent(day, new HashMap<>());
                        dayMap.get(day).put(
                                String.format("%02d00", slot),
                                Map.of("subject", session.subject, "teacher", session.teacher));
                    }

                    // Update counts
                    subjectDayCounts.get(session.subject).put(day, currentCount + 1);
                    placed = true;
                    break;
                }

                if (!placed) {
                    sessionQueue.add(session); // Retry later
                }

                attempt++;
            }

            sectionTimetable.put(sectionName, dayMap);
        }

        return sectionTimetable;
    }

    public static void printTimetables(
            Map<String, Map<String, Map<String, Map<String, String>>>> timetables,
            List<String> weekDays, List<String> slotLabels,
            Map<String, Integer> durations) {

        for (Map.Entry<String, Map<String, Map<String, Map<String, String>>>> sectionEntry : timetables
                .entrySet()) {
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
                        new HashMap<>());

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

    public static void printTimeSlot(Map<String, String> entry, Map<String, Integer> durations) {
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

    public static void saveToJson(
            Map<String, Map<String, Map<String, Map<String, String>>>> timetables,
            String filename,
            Map<String, Integer> durations) {
        try (FileWriter file = new FileWriter(filename)) {
            String json = "{\n";
            boolean firstSection = true;

            for (Map.Entry<String, Map<String, Map<String, Map<String, String>>>> section : timetables.entrySet()) {
                if (!firstSection) {
                    json += ",\n";
                }
                firstSection = false;

                json += "  \"" + section.getKey() + "\": {\n";
                boolean firstDay = true;

                for (Map.Entry<String, Map<String, Map<String, String>>> day : section.getValue().entrySet()) {
                    if (!firstDay) {
                        json += ",\n";
                    }
                    firstDay = false;

                    json += "    \"" + day.getKey() + "\": {\n";
                    boolean firstTime = true;

                    for (Map.Entry<String, Map<String, String>> time : day.getValue().entrySet()) {
                        if (!firstTime) {
                            json += ",\n";
                        }
                        firstTime = false;

                        json += "      \"" + time.getKey() + "\": {\n";
                        json += "        \"subject\": \"" + time.getValue().get("subject") + "\",\n";
                        json += "        \"teacher\": \"" + time.getValue().get("teacher") + "\",\n";
                        json += "        \"duration\": " + durations.get(time.getValue().get("subject")) + "\n";
                        json += "      }";
                    }
                    json += "\n    }";
                }
                json += "\n  }";
            }
            json += "\n}";

            file.write(json);
            System.out.println("✅ Timetable saved to " + filename);
        } catch (IOException e) {
            System.err.println("❌ Error saving file: " + e.getMessage());
        }
    }

    /**
     * Helper class to represent a subject session
     */
    static class SubjectSession {
        String subject;
        int duration;
        String teacher;

        SubjectSession(String subject, int duration, String teacher) {
            this.subject = subject;
            this.duration = duration;
            this.teacher = teacher;
        }
    }
}