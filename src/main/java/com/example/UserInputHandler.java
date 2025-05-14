package com.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class UserInputHandler {
    private final Scanner scanner;

    public UserInputHandler() {
        this.scanner = new Scanner(System.in);
    }

    public List<String> getSubjects() {
        System.out.println("\nEnter subjects (comma separated, e.g.: Math,English,Biology):");
        String input = scanner.nextLine();
        String[] subjectArray = input.split(",");
        List<String> subjects = new ArrayList<>();
        for (String subject : subjectArray) {
            subjects.add(subject.trim());
        }
        return subjects;
    }

    public Map<String, Integer> getDurations(List<String> subjects) {
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

    public Map<String, Integer> getSessionsPerWeek(List<String> subjects) {
        Map<String, Integer> sessions = new HashMap<>();
        System.out.println("\nEnter number of sessions per week for each subject:");
        for (String subject : subjects) {
            System.out.print(subject + ": ");
            sessions.put(subject, scanner.nextInt());
            scanner.nextLine(); // consume newline
        }
        return sessions;
    }

    public Map<String, List<String>> getTeachers(List<String> subjects) {
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

    public List<String> getWeekDays() {
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

    public void generateOrEditInputFile() {
        System.out.println("\n📝 Generate/Edit Input JSON File");
        System.out.println("===============================");

        // Try to load existing file if it exists
        JSONObject inputJson = new JSONObject();
        System.out.print("Enter filename to load/edit (or leave blank to create new): ");
        String filename = scanner.nextLine();

        if (!filename.isEmpty()) {
            try {
                org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
                inputJson = (JSONObject) parser.parse(new java.io.FileReader(filename));
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

    public int getMenuChoice() {
        System.out.println("\nChoose an option:");
        System.out.println("1. Generate new timetable (interactive input)");
        System.out.println("2. Generate new timetable (from JSON input file)");
        System.out.println("3. Print existing timetable from JSON file");
        System.out.println("4. Generate/edit input JSON file");
        System.out.println("5. Exit");
        System.out.print("Enter your choice (1-5): ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return choice;
    }

    public String getFilename(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int getNumberOfSections() {
        System.out.print("Enter number of sections: ");
        int numSections = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return numSections;
    }
} 