package util;

import model.*;
import java.io.*;
import java.util.*;

public class CSVParser {
    
    public static Map<String, Teacher> parseTeachers(InputStream inputStream) throws IOException {
        Map<String, Teacher> teachers = new HashMap<>();
        Map<String, Subject> subjects = new HashMap<>();
        
        // Initialize default subjects with durations
        subjects.put("JAVA", new Subject("JAVA", 2));
        subjects.put("DSA", new Subject("DSA", 2));
        subjects.put("LINUX", new Subject("LINUX", 2));
        subjects.put("MATH", new Subject("MATH", 1));
        subjects.put("OOSE", new Subject("OOSE", 1));
        subjects.put("BACKEND", new Subject("BACKEND", 1));
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        
        // Skip header
        reader.readLine();
        
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                String teacherName = parts[0].trim();
                String subjectName = parts[1].trim();
                
                // Get or create subject
                Subject subject = subjects.get(subjectName.toUpperCase());
                if (subject == null) {
                    // Default to 1 hour for unknown subjects
                    subject = new Subject(subjectName, 1);
                    subjects.put(subjectName.toUpperCase(), subject);
                }
                
                // Create teacher
                Teacher teacher = new Teacher(teacherName, subject);
                teachers.put(teacherName, teacher);
            }
        }
        
        return teachers;
    }
    
    public static Map<String, Subject> parseSubjects() {
        Map<String, Subject> subjects = new HashMap<>();
        
        // Create subjects with appropriate durations
        subjects.put("JAVA", new Subject("JAVA", 2));
        subjects.put("DSA", new Subject("DSA", 2));
        subjects.put("LINUX", new Subject("LINUX", 2));
        subjects.put("MATH", new Subject("MATH", 1));
        subjects.put("OOSE", new Subject("OOSE", 1));
        subjects.put("BACKEND", new Subject("BACKEND", 1));
        
        return subjects;
    }
    
    public static List<Classroom> parseClassRooms(int count) {
        List<Classroom> classRooms = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Classroom classRoom = new Classroom();
            classRoom.setName("Class " + (i + 1));
            classRooms.add(classRoom);
        }
        
        return classRooms;
    }
}
