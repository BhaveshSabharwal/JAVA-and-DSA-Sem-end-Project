package model;

public class TimeSlot {
    private int day;
    private int startTime;
    private int endTime;
    
    public TimeSlot(int day, int startTime, int endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public int getDay() {
        return day;
    }
    
    public void setDay(int day) {
        this.day = day;
    }
    
    public int getStartTime() {
        return startTime;
    }
    
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }
    
    public int getEndTime() {
        return endTime;
    }
    
    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }
    
    // Add compatibility methods for ValidationUtil
    public int getStartHour() {
        return startTime;
    }
    
    public int getEndHour() {
        return endTime;
    }
    
    public boolean overlaps(TimeSlot other) {
        if (this.day != other.day) {
            return false;
        }
        
        return (this.startTime <= other.endTime && this.endTime >= other.startTime);
    }
    
    @Override
    public String toString() {
        return "Day: " + day + ", Time: " + startTime + "-" + endTime;
    }
}
