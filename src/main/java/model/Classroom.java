package model;

public class Classroom {
    private String name;

    // Default constructor
    public Classroom() {
        this.name = "Default"; // Set default name
    }

    // Constructor with name
    public Classroom(String name) {
        this.name = name;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Setter for name
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Classroom: " + name;
    }
}