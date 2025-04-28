package model;

public class TimetableEntry {
    private Teacher teacher;
    private Subject subject;
    private TimeSlot timeSlot;
    private Classroom classRoom; // Add classRoom field for compatibility
    
    public TimetableEntry(Teacher teacher, Subject subject, TimeSlot timeSlot) {
        this.teacher = teacher;
        this.subject = subject;
        this.timeSlot = timeSlot;
        this.classRoom = new Classroom(); // Default classroom
        this.classRoom.setName("Default");
    }
    
    public TimetableEntry(Teacher teacher, Subject subject, TimeSlot timeSlot, Classroom classRoom) {
        this.teacher = teacher;
        this.subject = subject;
        this.timeSlot = timeSlot;
        this.classRoom = classRoom;
    }
    
    public Teacher getTeacher() {
        return teacher;
    }
    
    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }
    
    public Subject getSubject() {
        return subject;
    }
    
    public void setSubject(Subject subject) {
        this.subject = subject;
    }
    
    public TimeSlot getTimeSlot() {
        return timeSlot;
    }
    
    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }
    
    // Add compatibility method for ValidationUtil
    public Classroom getClassRoom() {
        return classRoom;
    }
    
    public void setClassRoom(Classroom classRoom) {
        this.classRoom = classRoom;
    }
    
    @Override
    public String toString() {
        return "Subject: " + subject.getName() + 
               ", Teacher: " + teacher.getName() + 
               ", " + timeSlot;
    }
}
