package com.example;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TimetableFileHandler {
    public void saveToJson(
            Map<String, Map<String, Map<String, Map<String, String>>>> timetables,
            String filename,
            Map<String, Integer> durations) throws IOException {
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
        }
    }

    public Map<String, Object> loadInputFile(String filename) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject inputJson = (JSONObject) parser.parse(new FileReader(filename));

        List<String> subjects = new ArrayList<>();
        Map<String, Integer> durations = new HashMap<>();
        Map<String, Integer> sessionsPerWeek = new HashMap<>();
        Map<String, List<String>> teachers = new HashMap<>();

        JSONArray subjectsArray = (JSONArray) inputJson.get("subjects");
        for (Object subjObj : subjectsArray) {
            JSONObject subject = (JSONObject) subjObj;
            String name = (String) subject.get("name");
            subjects.add(name);
            durations.put(name, ((Long) subject.get("duration")).intValue());
            sessionsPerWeek.put(name, ((Long) subject.get("sessions")).intValue());

            JSONArray teachersArray = (JSONArray) subject.get("teachers");
            List<String> teacherList = new ArrayList<>();
            for (Object teacher : teachersArray) {
                teacherList.add((String) teacher);
            }
            teachers.put(name, teacherList);
        }

        int daysPerWeek = ((Long) inputJson.get("days_per_week")).intValue();
        int numSections = ((Long) inputJson.get("sections")).intValue();

        List<String> weekDays = new ArrayList<>();
        String[] defaultDays = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" };
        for (int i = 0; i < daysPerWeek && i < defaultDays.length; i++) {
            weekDays.add(defaultDays[i]);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("subjects", subjects);
        result.put("durations", durations);
        result.put("sessionsPerWeek", sessionsPerWeek);
        result.put("teachers", teachers);
        result.put("weekDays", weekDays);
        result.put("numSections", numSections);

        return result;
    }

    public Map<String, Map<String, Map<String, Map<String, String>>>> loadTimetable(String filename) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject timetableJson = (JSONObject) parser.parse(new FileReader(filename));

        Map<String, Map<String, Map<String, Map<String, String>>>> timetables = new HashMap<>();

        for (Object sectionEntry : timetableJson.entrySet()) {
            Map.Entry<String, JSONObject> entry = (Map.Entry<String, JSONObject>) sectionEntry;
            String sectionName = entry.getKey();
            JSONObject sectionData = entry.getValue();

            Map<String, Map<String, Map<String, String>>> sectionMap = new HashMap<>();
            for (Object dayEntry : sectionData.entrySet()) {
                Map.Entry<String, JSONObject> day = (Map.Entry<String, JSONObject>) dayEntry;
                String dayName = day.getKey();
                JSONObject dayData = day.getValue();

                Map<String, Map<String, String>> dayMap = new HashMap<>();
                for (Object timeEntry : dayData.entrySet()) {
                    Map.Entry<String, JSONObject> time = (Map.Entry<String, JSONObject>) timeEntry;
                    String timeKey = time.getKey();
                    JSONObject classData = time.getValue();

                    Map<String, String> classMap = new HashMap<>();
                    classMap.put("subject", (String) classData.get("subject"));
                    classMap.put("teacher", (String) classData.get("teacher"));
                    dayMap.put(timeKey, classMap);
                }
                sectionMap.put(dayName, dayMap);
            }
            timetables.put(sectionName, sectionMap);
        }

        return timetables;
    }
}