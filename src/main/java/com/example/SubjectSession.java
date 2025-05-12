package com.example;

public class SubjectSession {
    private final String subject;
    private final int duration;
    private final String teacher;

    public SubjectSession(String subject, int duration, String teacher) {
        this.subject = subject;
        this.duration = duration;
        this.teacher = teacher;
    }

    public String getSubject() {
        return subject;
    }

    public int getDuration() {
        return duration;
    }

    public String getTeacher() {
        return teacher;
    }
} 