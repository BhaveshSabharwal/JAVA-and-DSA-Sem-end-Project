package com.example;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
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

public class Main {
    private static final int START_TIME = 9;
    private static final int END_TIME = 17;
    private static final int RECESS_START = 12;
    private static final int RECESS_END = 13;

    private static final Random random = new Random();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("🎓 Student Timetable Generator 🎓");
        System.out.println("=================================");

        UserInputHandler inputHandler = new UserInputHandler();
        TimetableGenerator timetableGenerator = new TimetableGenerator();
        TimetablePrinter timetablePrinter = new TimetablePrinter();
        TimetableFileHandler fileHandler = new TimetableFileHandler();
        deleteTimetable deleteTimetable = new deleteTimetable();

        while (true) {
            int choice = inputHandler.getMenuChoice();

            try {
                switch (choice) {
                    case 1:
                        generateTimetableInteractive(inputHandler, timetableGenerator, timetablePrinter, fileHandler);
                        break;
                    case 2:
                        generateTimetableFromFile(inputHandler, timetableGenerator, timetablePrinter, fileHandler);
                        break;
                    case 3:
                        printExistingTimetable(inputHandler, timetablePrinter, fileHandler);
                        break;
                    case 4:
                        inputHandler.generateOrEditInputFile();
                        break;
                    case 5:
                        deleteTimetableFile(inputHandler, fileHandler);
                        break;
                    case 6:
                        System.out.println("Exiting program. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static void generateTimetableInteractive(
            UserInputHandler inputHandler,
            TimetableGenerator timetableGenerator,
            TimetablePrinter timetablePrinter,
            TimetableFileHandler fileHandler) throws Exception {

        List<String> subjects = inputHandler.getSubjects();
        Map<String, Integer> durations = inputHandler.getDurations(subjects);
        Map<String, Integer> sessionsPerWeek = inputHandler.getSessionsPerWeek(subjects);
        Map<String, List<String>> teachers = inputHandler.getTeachers(subjects);
        List<String> weekDays = inputHandler.getWeekDays();
        int numSections = inputHandler.getNumberOfSections();

        List<Integer> dailySlots = timetableGenerator.generateDailySlots();
        List<String> slotLabels = timetableGenerator.generateSlotLabels(dailySlots);

        Map<String, Map<String, Map<String, Map<String, String>>>> timetables = timetableGenerator.generateTimetable(
                subjects, durations, sessionsPerWeek, teachers, weekDays, numSections);

        timetablePrinter.printTimetables(timetables, weekDays, slotLabels, durations);

        String filename = inputHandler.getFilename("Enter filename to save (e.g., timetables.json): ");
        fileHandler.saveToJson(timetables, filename, durations);

        System.out.println("\n✅ Timetable generation complete!");
    }

    private static void generateTimetableFromFile(
            UserInputHandler inputHandler,
            TimetableGenerator timetableGenerator,
            TimetablePrinter timetablePrinter,
            TimetableFileHandler fileHandler) throws Exception {

        String inputFile = inputHandler.getFilename("Enter input JSON filename (e.g., input.json): ");
        Map<String, Object> inputData = fileHandler.loadInputFile(inputFile);

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

        List<Integer> dailySlots = timetableGenerator.generateDailySlots();
        List<String> slotLabels = timetableGenerator.generateSlotLabels(dailySlots);

        Map<String, Map<String, Map<String, Map<String, String>>>> timetables = timetableGenerator.generateTimetable(
                subjects, durations, sessionsPerWeek, teachers, weekDays, numSections);

        timetablePrinter.printTimetables(timetables, weekDays, slotLabels, durations);

        String filename = inputHandler.getFilename("Enter filename to save (e.g., timetables.json): ");
        fileHandler.saveToJson(timetables, filename, durations);

        System.out.println("\n✅ Timetable generation complete!");
    }

    private static void printExistingTimetable(
            UserInputHandler inputHandler,
            TimetablePrinter timetablePrinter,
            TimetableFileHandler fileHandler) throws Exception {

        String filename = inputHandler.getFilename("Enter timetable JSON filename to print (e.g., timetable.json): ");
        Map<String, Map<String, Map<String, Map<String, String>>>> timetables = fileHandler.loadTimetable(filename);

        // Extract week days from the timetable
        List<String> weekDays = timetables.values().stream()
                .flatMap(section -> section.keySet().stream())
                .distinct()
                .sorted()
                .toList();

        // Generate slot labels
        List<Integer> dailySlots = List.of(9, 10, 11, 13, 14, 15, 16);
        List<String> slotLabels = dailySlots.stream()
                .map(t -> String.format("%02d00 - %02d00", t, t + 1))
                .toList();

        // Extract durations from the timetable
        Map<String, Integer> durations = new java.util.HashMap<>();
        timetables.values().forEach(section -> section.values().forEach(day -> day.values().forEach(time -> {
            String subject = time.get("subject");
            if (!durations.containsKey(subject)) {
                durations.put(subject, 1); // Default duration
            }
        })));

        timetablePrinter.printTimetables(timetables, weekDays, slotLabels, durations);
    }

    private static void generateOrEditInputFile() {
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
    private static void deleteTimetableFile(UserInputHandler inputHandler,
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

    private static List<String> getSubjects() {
        System.out.println("\nEnter subjects (comma separated, e.g.: Math,English,Biology):");
        String input = scanner.nextLine();
        String[] subjectArray = input.split(",");
        List<String> subjects = new ArrayList<>();
        for (String subject : subjectArray) {
            subjects.add(subject.trim());
        }
        return subjects;
    }

    private static Map<String, Integer> getDurations(List<String> subjects) {
        Map<String, Integer> durations = new HashMap<>();
        System.out.println("\nEnter duration (in hours) for each subject (1 or 2 only):");
        for (String subject : subjects) {
            int duration;
            do {
                System.out.print(subject + ": ");
                duration = scanner.nextInt();
                if (duration != 1 && duration != 2) {
                    System.out.println("Please enter only 1 or 2 for duration");
                }
            } while (duration != 1 && duration != 2);
            durations.put(subject, duration);
            scanner.nextLine(); // consume newline
        }
        return durations;
    }

    private static Map<String, Integer> getSessionsPerWeek(List<String> subjects) {
        Map<String, Integer> sessions = new HashMap<>();
        System.out.println("\nEnter number of sessions per week for each subject:");
        for (String subject : subjects) {
            System.out.print(subject + ": ");
            sessions.put(subject, scanner.nextInt());
            scanner.nextLine(); // consume newline
        }
        return sessions;
    }

    private static Map<String, List<String>> getTeachers(List<String> subjects) {
        Map<String, List<String>> teachers = new HashMap<>();
        System.out.println("\nEnter teachers for each subject (comma separated):");
        for (String subject : subjects) {
            System.out.print(subject + ": ");
            String input = scanner.nextLine();
            String[] teacherArray = input.split(",");
            List<String> teacherList = new ArrayList<>();
            for (String teacher : teacherArray) {
                teacherList.add(teacher.trim());
            }
            teachers.put(subject, teacherList);
        }
        return teachers;
    }

    private static List<String> getWeekDays() {
        System.out.println("\nEnter week days (comma separated, default: Monday,Tuesday,Wednesday,Thursday,Friday):");
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

    private static List<Integer> generateDailySlots() {
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

    private static List<String> generateSlotLabels(List<Integer> slots) {
        List<String> labels = new ArrayList<>();
        for (int t : slots) {
            labels.add(String.format("%02d00 - %02d00", t, t + 1));
        }
        return labels;
    }

    private static Map<String, Map<String, Map<String, Map<String, String>>>> assignSubjectsWithTeachers(
            List<String> subjects, Map<String, Integer> durations, Map<String, Integer> sessionsPerWeek,
            Map<String, List<String>> teachers, List<String> weekDays, int numSections, List<Integer> dailySlots) {

        Map<String, Map<String, Map<String, Map<String, String>>>> sectionTimetable = new HashMap<>();

        for (int sec = 1; sec <= numSections; sec++) {
            String sectionName = "Section " + sec;
            Map<String, Map<String, Map<String, String>>> dayMap = new HashMap<>();
            Set<String> usedSlots = new HashSet<>();
            Map<String, Map<String, Integer>> subjectDayCounts = new HashMap<>(); // Track subject counts per day
            List<SubjectSession> subjectSessions = new ArrayList<>();

            // Initialize subjectDayCounts map
            for (String subject : subjects) {
                subjectDayCounts.put(subject, new HashMap<>());
                for (String day : weekDays) {
                    subjectDayCounts.get(subject).put(day, 0);
                }
            }

            // Create all required subject sessions
            for (String subj : subjects) {
                int sessions = sessionsPerWeek.get(subj);
                for (int i = 0; i < sessions; i++) {
                    String teacher = teachers.get(subj).get(random.nextInt(teachers.get(subj).size()));
                    subjectSessions.add(new SubjectSession(subj, durations.get(subj), teacher));
                }
            }

            // Shuffle the sessions
            Collections.shuffle(subjectSessions);

            // Place each session in the timetable
            for (SubjectSession session : subjectSessions) {
                boolean placed = false;
                int attempts = 0;

                while (!placed && attempts < 100) {
                    String day = weekDays.get(random.nextInt(weekDays.size()));

                    // Check if this subject already has max classes on this day
                    int currentCount = subjectDayCounts.get(session.subject).get(day);
                    if ((session.duration == 1 && currentCount >= 2) ||
                            (session.duration == 2 && currentCount >= 1)) {
                        attempts++;
                        continue;
                    }

                    // For 2-hour classes, we need to find two consecutive slots not at the end
                    int startIdx;
                    if (session.duration == 2) {
                        if (dailySlots.size() < 2) {
                            attempts++;
                            continue;
                        }

                        // Find all possible starting positions for 2-hour classes
                        List<Integer> possibleStarts = new ArrayList<>();
                        for (int i = 0; i < dailySlots.size() - 1; i++) {
                            // Ensure we're not at the last period
                            if (i >= dailySlots.size() - 2) {
                                continue; // Skip last two slots for 2-hour classes
                            }

                            int firstSlot = dailySlots.get(i);
                            int secondSlot = dailySlots.get(i + 1);
                            if (firstSlot + 1 == secondSlot) { // Check if consecutive
                                String timeKey1 = day + "-" + String.format("%02d00", firstSlot);
                                String timeKey2 = day + "-" + String.format("%02d00", secondSlot);
                                if (!usedSlots.contains(timeKey1) && !usedSlots.contains(timeKey2)) {
                                    possibleStarts.add(i);
                                }
                            }
                        }

                        if (possibleStarts.isEmpty()) {
                            attempts++;
                            continue;
                        }

                        startIdx = possibleStarts.get(random.nextInt(possibleStarts.size()));
                    } else {
                        // For 1-hour classes, just find any available slot
                        List<Integer> availableSlots = new ArrayList<>();
                        for (int i = 0; i < dailySlots.size(); i++) {
                            String timeKey = day + "-" + String.format("%02d00", dailySlots.get(i));
                            if (!usedSlots.contains(timeKey)) {
                                availableSlots.add(i);
                            }
                        }

                        if (availableSlots.isEmpty()) {
                            attempts++;
                            continue;
                        }

                        startIdx = availableSlots.get(random.nextInt(availableSlots.size()));
                    }

                    // Check if all slots in range are available
                    boolean conflict = false;
                    List<Integer> slotRange = dailySlots.subList(startIdx, startIdx + session.duration);
                    for (int slot : slotRange) {
                        String timeKey = day + "-" + String.format("%02d00", slot);
                        if (usedSlots.contains(timeKey)) {
                            conflict = true;
                            break;
                        }
                    }

                    if (conflict) {
                        attempts++;
                        continue;
                    }

                    // Place the session
                    for (int slot : slotRange) {
                        String timeKey = day + "-" + String.format("%02d00", slot);
                        usedSlots.add(timeKey);

                        dayMap.putIfAbsent(day, new HashMap<>());
                        dayMap.get(day).put(
                                String.format("%02d00", slot),
                                Map.of(
                                        "subject", session.subject,
                                        "teacher", session.teacher));
                    }

                    // Update subject count for this day
                    subjectDayCounts.get(session.subject).put(day,
                            subjectDayCounts.get(session.subject).get(day) + 1);
                    placed = true;
                }
            }

            sectionTimetable.put(sectionName, dayMap);
        }

        return sectionTimetable;
    }

    private static void printTimetables(
            Map<String, Map<String, Map<String, Map<String, String>>>> timetables,
            List<String> weekDays, List<String> slotLabels,
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

    private static void printTimeSlot(Map<String, String> entry, Map<String, Integer> durations) {
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

    private static void saveToJson(
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