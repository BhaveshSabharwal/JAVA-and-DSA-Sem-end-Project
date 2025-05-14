package com.example;

import java.util.*;

public class TimetableGenerator {
    private static final int START_TIME = 9;
    private static final int END_TIME = 17;
    private static final int RECESS_START = 12;
    private static final int RECESS_END = 13;
    private final Random random;

    public TimetableGenerator() {
        this.random = new Random();
    }

    public List<Integer> generateDailySlots() {
        List<Integer> slots = new ArrayList<>();
        int t = START_TIME;
        while (t < END_TIME) {
            if (t == RECESS_START) {
                t = RECESS_END;
                continue;
            }
            slots.add(t);
            t++;
        }
        return slots;
    }

    public List<String> generateSlotLabels(List<Integer> slots) {
        List<String> labels = new ArrayList<>();
        for (int t : slots) {
            labels.add(String.format("%02d00 - %02d00", t, t + 1));
        }
        return labels;
    }

    public Map<String, Map<String, Map<String, Map<String, String>>>> generateTimetable(
            List<String> subjects,
            Map<String, Integer> durations,
            Map<String, Integer> sessionsPerWeek,
            Map<String, List<String>> teachers,
            List<String> weekDays,
            int numSections) {

        List<Integer> dailySlots = generateDailySlots();
        Map<String, Map<String, Map<String, Map<String, String>>>> sectionTimetable = new HashMap<>();

        for (int sec = 1; sec <= numSections; sec++) {
            String sectionName = "Section " + sec;
            Map<String, Map<String, Map<String, String>>> dayMap = new HashMap<>();
            Set<String> usedSlots = new HashSet<>();
            Map<String, Map<String, Integer>> subjectDayCounts = new HashMap<>();
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
                    int currentCount = subjectDayCounts.get(session.getSubject()).get(day);
                    if ((session.getDuration() == 1 && currentCount >= 2) ||
                            (session.getDuration() == 2 && currentCount >= 1)) {
                        attempts++;
                        continue;
                    }

                    // For 2-hour classes, we need to find two consecutive slots not at the end
                    int startIdx;
                    if (session.getDuration() == 2) {
                        if (dailySlots.size() < 2) {
                            attempts++;
                            continue;
                        }

                        // Find all possible starting positions for 2-hour classes
                        List<Integer> possibleStarts = new ArrayList<>();
                        for (int i = 0; i < dailySlots.size() - 1; i++) {
                            if (i >= dailySlots.size() - 2) {
                                continue;
                            }

                            int firstSlot = dailySlots.get(i);
                            int secondSlot = dailySlots.get(i + 1);
                            if (firstSlot + 1 == secondSlot) {
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

                    boolean conflict = false;
                    List<Integer> slotRange = dailySlots.subList(startIdx, startIdx + session.getDuration());
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
                                        "subject", session.getSubject(),
                                        "teacher", session.getTeacher()));
                    }

                    subjectDayCounts.get(session.getSubject()).put(day,
                            subjectDayCounts.get(session.getSubject()).get(day) + 1);
                    placed = true;
                }
            }

            sectionTimetable.put(sectionName, dayMap);
        }

        return sectionTimetable;
    }
} 