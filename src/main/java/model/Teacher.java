package model;

public class Teacher {
    private String name;
    private Subject subject;
    
    public Teacher(String name, Subject subject) {
        this.name = name;
        this.subject = subject;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Subject getSubject() {
        return subject;
    }
    
    public void setSubject(Subject subject) {
        this.subject = subject;
    }
    
    @Override
    public String toString() {
        return name + " (" + subject.getName() + ")";
    }
}
