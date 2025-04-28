package model;

import java.util.*;

public class Timetable {
    private String name;
    private List<TimetableEntry> entries;
    
    public Timetable() {
        this.entries = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<TimetableEntry> getEntries() {
        return entries;
    }
    
    public void setEntries(List<TimetableEntry> entries) {
        this.entries = entries;
    }
    
    public void addEntry(TimetableEntry entry) {
        this.entries.add(entry);
    }
    
    public TimetableEntry getEntryByDayAndTime(int day, int timeSlot) {
        for (TimetableEntry entry : entries) {
            TimeSlot slot = entry.getTimeSlot();
            if (slot.getDay() == day && 
                timeSlot >= slot.getStartTime() && 
                timeSlot <= slot.getEndTime()) {
                return entry;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Timetable: ").append(name).append("\n");
        
        for (TimetableEntry entry : entries) {
            sb.append(entry).append("\n");
        }
        
        return sb.toString();
    }
}
