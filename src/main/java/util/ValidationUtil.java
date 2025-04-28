package util;

import model.*;
import java.util.*;

public class ValidationUtil {
    
    // Constants for validation
    private static final int MAX_CLASSES_PER_DAY = 4;
    private static final int MIN_CLASSES_PER_DAY = 3;
    private static final int DAYS_PER_WEEK = 5;
    private static final int LUNCH_BREAK_SLOT = 4; // 1 PM to 2 PM
    
    /**
     * Validates that each subject appears with the correct weekly frequency
     */
    public static boolean validateSubjectFrequency(Timetable timetable, List<Subject> subjects) {
        Map<String, Integer> subjectCounts = new HashMap<>();
        
        // Initialize counts
        for (Subject subject : subjects) {
            subjectCounts.put(subject.getName().toUpperCase(), 0);
        }
        
        // Count occurrences
        for (TimetableEntry entry : timetable.getEntries()) {
            String subjectName = entry.getSubject().getName().toUpperCase();
            subjectCounts.put(subjectName, subjectCounts.getOrDefault(subjectName, 0) + 1);
        }
        
        // Validate counts
        for (Subject subject : subjects) {
            String subjectName = subject.getName().toUpperCase();
            int count = subjectCounts.getOrDefault(subjectName, 0);
            int expected = subject.getWeeklyFrequency();
            
            if (count != expected) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates that there are no double bookings of rooms
     */
    public static boolean validateNoDoubleBookingRooms(Timetable timetable) {
        List<TimetableEntry> entries = timetable.getEntries();
        
        for (int i = 0; i < entries.size(); i++) {
            TimetableEntry entry = entries.get(i);
            TimeSlot timeSlot = entry.getTimeSlot();
            Classroom classRoom = entry.getClassRoom();
            
            for (int j = i + 1; j < entries.size(); j++) {
                TimetableEntry otherEntry = entries.get(j);
                TimeSlot otherSlot = otherEntry.getTimeSlot();
                Classroom otherRoom = otherEntry.getClassRoom();
                
                if (classRoom.getName().equals(otherRoom.getName()) && 
                    timeSlot.getDay() == otherSlot.getDay() && 
                    timeSlot.overlaps(otherSlot)) {
                    
                    System.out.println("Room double booking: " + classRoom.getName() + 
                                      " at day " + timeSlot.getDay() + 
                                      " time " + timeSlot.getStartHour() + "-" + timeSlot.getEndHour());
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Validates that there are no double bookings of teachers
     */
    public static boolean validateNoDoubleBookingTeachers(Timetable timetable) {
        List<TimetableEntry> entries = timetable.getEntries();
        
        for (int i = 0; i < entries.size(); i++) {
            TimetableEntry entry = entries.get(i);
            TimeSlot timeSlot = entry.getTimeSlot();
            Teacher teacher = entry.getTeacher();
            
            for (int j = i + 1; j < entries.size(); j++) {
                TimetableEntry otherEntry = entries.get(j);
                TimeSlot otherSlot = otherEntry.getTimeSlot();
                Teacher otherTeacher = otherEntry.getTeacher();
                
                if (teacher.getName().equals(otherTeacher.getName()) && 
                    timeSlot.getDay() == otherSlot.getDay() && 
                    timeSlot.overlaps(otherSlot)) {
                    
                    System.out.println("Teacher double booking: " + teacher.getName() + 
                                      " at day " + timeSlot.getDay() + 
                                      " time " + timeSlot.getStartHour() + "-" + timeSlot.getEndHour());
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Validates that there are no double subjects on the same day
     */
    public static boolean validateNoDoubleSubjectsPerDay(Timetable timetable) {
        // Group entries by day
        Map<Integer, Set<String>> subjectsPerDay = new HashMap<>();
        
        for (TimetableEntry entry : timetable.getEntries()) {
            int day = entry.getTimeSlot().getDay();
            String subject = entry.getSubject().getName().toUpperCase();
            
            if (!subjectsPerDay.containsKey(day)) {
                subjectsPerDay.put(day, new HashSet<>());
            }
            
            Set<String> daySubjects = subjectsPerDay.get(day);
            if (daySubjects.contains(subject)) {
                System.out.println("Double subject on day " + day + ": " + subject);
                return false;
            }
            
            daySubjects.add(subject);
        }
        
        return true;
    }
    
    /**
     * Validates that there are 3-4 classes per day
     */
    public static boolean validateClassesPerDay(Timetable timetable) {
        // Group entries by day
        Map<Integer, Integer> classesPerDay = new HashMap<>();
        
        for (TimetableEntry entry : timetable.getEntries()) {
            int day = entry.getTimeSlot().getDay();
            classesPerDay.put(day, classesPerDay.getOrDefault(day, 0) + 1);
        }
        
        // Check each day
        for (int day = 0; day < DAYS_PER_WEEK; day++) {
            int count = classesPerDay.getOrDefault(day, 0);
            if (count < MIN_CLASSES_PER_DAY || count > MAX_CLASSES_PER_DAY) {
                System.out.println("Invalid classes per day: " + count + " on day " + day);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates that the lunch break (1 PM to 2 PM) is respected
     */
    public static boolean validateLunchBreak(Timetable timetable) {
        for (TimetableEntry entry : timetable.getEntries()) {
            TimeSlot timeSlot = entry.getTimeSlot();
            
            // Check if the time slot overlaps with lunch break
            if (timeSlot.getStartTime() <= LUNCH_BREAK_SLOT && timeSlot.getEndTime() >= LUNCH_BREAK_SLOT) {
                System.out.println("Class scheduled during lunch break: " + 
                                  entry.getSubject().getName() + " at day " + 
                                  timeSlot.getDay() + " time " + 
                                  timeSlot.getStartTime() + "-" + timeSlot.getEndTime());
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Runs all validation checks on a timetable
     */
    public static boolean validateTimetable(Timetable timetable, List<Subject> subjects) {
        boolean valid = true;
        
        valid = valid && validateSubjectFrequency(timetable, subjects);
        valid = valid && validateNoDoubleBookingRooms(timetable);
        valid = valid && validateNoDoubleBookingTeachers(timetable);
        valid = valid && validateNoDoubleSubjectsPerDay(timetable);
        valid = valid && validateClassesPerDay(timetable);
        valid = valid && validateLunchBreak(timetable);
        
        return valid;
    }
}
