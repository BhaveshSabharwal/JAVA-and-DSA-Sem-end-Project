package util;

import model.*;
import java.util.*;

public class TimetableGenerator {
    
    private static final int DAYS_PER_WEEK = 5; // Monday to Friday
    private static final int SLOTS_PER_DAY = 7; // 9 AM to 4 PM (7 hours)
    private static final int LUNCH_BREAK_SLOT = 4; // 1 PM to 2 PM
    
    // Class frequency requirements
    private static final Map<String, Integer> SUBJECT_FREQUENCY = new HashMap<>();
    static {
        SUBJECT_FREQUENCY.put("JAVA", 3);
        SUBJECT_FREQUENCY.put("DSA", 3);
        SUBJECT_FREQUENCY.put("MATH", 3);
        SUBJECT_FREQUENCY.put("OOSE", 3);
        SUBJECT_FREQUENCY.put("LINUX", 2);
        SUBJECT_FREQUENCY.put("BACKEND", 2);
    }
    
    // Teacher workload constraints
    private static final Map<String, Integer> TEACHER_MAX_CLASSES = new HashMap<>();
    static {
        TEACHER_MAX_CLASSES.put("JAVA", 3);
        TEACHER_MAX_CLASSES.put("DSA", 3);
        TEACHER_MAX_CLASSES.put("LINUX", 3);
        TEACHER_MAX_CLASSES.put("MATH", 6);
        TEACHER_MAX_CLASSES.put("OOSE", 6);
        TEACHER_MAX_CLASSES.put("BACKEND", 6);
    }
    
    public List<Timetable> generateTimetables(List<Teacher> teachers, List<Subject> subjects, int classCount) {
        List<Timetable> timetables = new ArrayList<>();
        
        // Create empty timetables for each class
        for (int i = 0; i < classCount; i++) {
            Timetable timetable = new Timetable();
            timetable.setName("Class " + (i + 1));
            timetables.add(timetable);
        }
        
        // Map to track teacher assignments
        Map<Teacher, Integer> teacherAssignments = new HashMap<>();
        for (Teacher teacher : teachers) {
            teacherAssignments.put(teacher, 0);
        }
        
        // Map to track subject frequency for each class
        Map<Integer, Map<String, Integer>> classSubjectCount = new HashMap<>();
        for (int i = 0; i < classCount; i++) {
            classSubjectCount.put(i, new HashMap<>());
            for (Subject subject : subjects) {
                classSubjectCount.get(i).put(subject.getName().toUpperCase(), 0);
            }
        }
        
        // Generate timetable for each class
        Random random = new Random();
        
        // First, assign the required subjects to each class
        for (int classIndex = 0; classIndex < classCount; classIndex++) {
            Timetable timetable = timetables.get(classIndex);
            Map<String, Integer> subjectCount = classSubjectCount.get(classIndex);
            
            // Try to assign subjects
            for (int day = 0; day < DAYS_PER_WEEK; day++) {
                // Track subjects assigned on this day to avoid duplicates
                Set<String> subjectsAssignedToday = new HashSet<>();
                
                // Track available slots for this day
                List<Integer> availableSlots = new ArrayList<>();
                for (int slot = 0; slot < SLOTS_PER_DAY; slot++) {
                    if (slot != LUNCH_BREAK_SLOT) { // Skip lunch break
                        availableSlots.add(slot);
                    }
                }
                
                // Shuffle available slots
                Collections.shuffle(availableSlots, random);
                
                // Try to assign 3-4 classes per day
                int classesForToday = 3 + random.nextInt(2); // 3 or 4
                int classesAssigned = 0;
                
                // Sort subjects by remaining frequency needed
                List<Subject> sortedSubjects = new ArrayList<>(subjects);
                Collections.sort(sortedSubjects, (s1, s2) -> {
                    String name1 = s1.getName().toUpperCase();
                    String name2 = s2.getName().toUpperCase();
                    
                    int freq1 = SUBJECT_FREQUENCY.getOrDefault(name1, 0) - subjectCount.getOrDefault(name1, 0);
                    int freq2 = SUBJECT_FREQUENCY.getOrDefault(name2, 0) - subjectCount.getOrDefault(name2, 0);
                    
                    return Integer.compare(freq2, freq1); // Higher remaining frequency first
                });
                
                for (Subject subject : sortedSubjects) {
                    String subjectName = subject.getName().toUpperCase();
                    
                    // Skip if subject already assigned today or reached weekly limit
                    if (subjectsAssignedToday.contains(subjectName) || 
                        subjectCount.getOrDefault(subjectName, 0) >= SUBJECT_FREQUENCY.getOrDefault(subjectName, 0)) {
                        continue;
                    }
                    
                    // Find a suitable teacher
                    Teacher selectedTeacher = null;
                    for (Teacher teacher : teachers) {
                        if (teacher.getSubject().getName().equalsIgnoreCase(subject.getName()) && 
                            teacherAssignments.get(teacher) < TEACHER_MAX_CLASSES.getOrDefault(
                                teacher.getSubject().getName().toUpperCase(), 6)) {
                            selectedTeacher = teacher;
                            break;
                        }
                    }
                    
                    if (selectedTeacher == null) {
                        continue; // No available teacher for this subject
                    }
                    
                    // Find suitable time slot
                    Integer selectedSlot = null;
                    for (Integer slot : availableSlots) {
                        // Check if there's enough consecutive slots for this subject
                        boolean canFit = true;
                        for (int i = 0; i < subject.getDuration(); i++) {
                            if (slot + i >= SLOTS_PER_DAY || slot + i == LUNCH_BREAK_SLOT || 
                                !availableSlots.contains(slot + i)) {
                                canFit = false;
                                break;
                            }
                        }
                        
                        if (canFit) {
                            selectedSlot = slot;
                            break;
                        }
                    }
                    
                    if (selectedSlot == null) {
                        continue; // No suitable time slot
                    }
                    
                    // Create time slot and entry
                    TimeSlot timeSlot = new TimeSlot(day, selectedSlot, selectedSlot + subject.getDuration() - 1);
                    TimetableEntry entry = new TimetableEntry(selectedTeacher, subject, timeSlot);
                    timetable.addEntry(entry);
                    
                    // Update tracking data
                    subjectsAssignedToday.add(subjectName);
                    subjectCount.put(subjectName, subjectCount.getOrDefault(subjectName, 0) + 1);
                    teacherAssignments.put(selectedTeacher, teacherAssignments.get(selectedTeacher) + 1);
                    
                    // Remove used slots
                    for (int i = 0; i < subject.getDuration(); i++) {
                        availableSlots.remove(Integer.valueOf(selectedSlot + i));
                    }
                    
                    classesAssigned++;
                    if (classesAssigned >= classesForToday) {
                        break;
                    }
                }
            }
        }
        
        return timetables;
    }
}
