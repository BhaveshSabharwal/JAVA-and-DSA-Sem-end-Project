package model;

public class Subject {
    private String name;
    private int duration; // in hours
    
    public Subject(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    // Add compatibility method for ValidationUtil
    public int getWeeklyFrequency() {
        String upperName = name.toUpperCase();
        if (upperName.equals("JAVA") || upperName.equals("DSA") || 
            upperName.equals("MATH") || upperName.equals("OOSE")) {
            return 3;
        } else if (upperName.equals("LINUX") || upperName.equals("BACKEND")) {
            return 2;
        }
        return 1; // Default for unknown subjects
    }
    
    @Override
    public String toString() {
        return name + " (" + duration + "h)";
    }
}
