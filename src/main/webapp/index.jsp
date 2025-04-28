<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="model.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Timetable Generator</title>
    <link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
    <div class="container">
        <h1>Timetable Generator</h1>
        
        <% 
            // Initialize session attributes if they don't exist
            if (session.getAttribute("teachers") == null) {
                session.setAttribute("teachers", new ArrayList<Teacher>());
            }
            if (session.getAttribute("subjects") == null) {
                session.setAttribute("subjects", new ArrayList<Subject>());
            }
            if (session.getAttribute("classCount") == null) {
                session.setAttribute("classCount", 1);
            }
            
            // Handle form submissions
            String action = request.getParameter("action");
            if (action != null) {
                if (action.equals("addTeacher")) {
                    String teacherName = request.getParameter("teacherName");
                    String subjectName = request.getParameter("subjectName");
                    
                    if (teacherName != null && !teacherName.trim().isEmpty() && 
                        subjectName != null && !subjectName.trim().isEmpty()) {
                        
                        ArrayList<Teacher> teachers = (ArrayList<Teacher>) session.getAttribute("teachers");
                        ArrayList<Subject> subjects = (ArrayList<Subject>) session.getAttribute("subjects");
                        
                        // Check if subject already exists
                        Subject subject = null;
                        for (Subject s : subjects) {
                            if (s.getName().equals(subjectName)) {
                                subject = s;
                                break;
                            }
                        }
                        
                        // Create new subject if it doesn't exist
                        if (subject == null) {
                            // Set duration based on subject name
                            int duration = 1; // default
                            if (subjectName.equalsIgnoreCase("JAVA") || 
                                subjectName.equalsIgnoreCase("DSA") || 
                                subjectName.equalsIgnoreCase("Linux")) {
                                duration = 2;
                            }
                            
                            subject = new Subject(subjectName, duration);
                            subjects.add(subject);
                        }
                        
                        // Create new teacher
                        Teacher teacher = new Teacher(teacherName, subject);
                        teachers.add(teacher);
                        
                        session.setAttribute("teachers", teachers);
                        session.setAttribute("subjects", subjects);
                    }
                } else if (action.equals("setClassCount")) {
                    String classCountStr = request.getParameter("classCount");
                    if (classCountStr != null && !classCountStr.trim().isEmpty()) {
                        try {
                            int classCount = Integer.parseInt(classCountStr);
                            if (classCount > 0) {
                                session.setAttribute("classCount", classCount);
                            }
                        } catch (NumberFormatException e) {
                            // Invalid input, ignore
                        }
                    }
                } else if (action.equals("generateTimetable")) {
                    // Redirect to timetable.jsp
                    response.sendRedirect("timetable.jsp");
                    return;
                } else if (action.equals("reset")) {
                    // Reset all data
                    session.setAttribute("teachers", new ArrayList<Teacher>());
                    session.setAttribute("subjects", new ArrayList<Subject>());
                    session.setAttribute("classCount", 1);
                }
            }
            
            ArrayList<Teacher> teachers = (ArrayList<Teacher>) session.getAttribute("teachers");
            int classCount = (Integer) session.getAttribute("classCount");
        %>
        
        <div class="form-container">
            <h2>Add Teacher</h2>
            <form method="post" action="index.jsp">
                <input type="hidden" name="action" value="addTeacher">
                <div class="form-group">
                    <label for="teacherName">Teacher Name:</label>
                    <input type="text" id="teacherName" name="teacherName" required>
                </div>
                <div class="form-group">
                    <label for="subjectName">Subject:</label>
                    <input type="text" id="subjectName" name="subjectName" required>
                    <small>Note: JAVA, DSA, Linux are 2-hour classes. Math, OOSE, Backend are 1-hour classes.</small>
                </div>
                <button type="submit" class="btn">Add Teacher</button>
            </form>
            
            <h2>Set Number of Classes</h2>
            <form method="post" action="index.jsp">
                <input type="hidden" name="action" value="setClassCount">
                <div class="form-group">
                    <label for="classCount">Number of Classes:</label>
                    <input type="number" id="classCount" name="classCount" min="1" value="<%= classCount %>" required>
                </div>
                <button type="submit" class="btn">Set Class Count</button>
            </form>
            
            <h2>Generate Timetable</h2>
            <form method="post" action="index.jsp">
                <input type="hidden" name="action" value="generateTimetable">
                <button type="submit" class="btn generate-btn" <%= teachers.isEmpty() ? "disabled" : "" %>>Generate Timetable</button>
            </form>
            
            <form method="post" action="index.jsp" class="reset-form">
                <input type="hidden" name="action" value="reset">
                <button type="submit" class="btn reset-btn">Reset All Data</button>
            </form>
        </div>
        
        <div class="data-display">
            <h2>Added Teachers</h2>
            <% if (teachers.isEmpty()) { %>
                <p>No teachers added yet.</p>
            <% } else { %>
                <table>
                    <thead>
                        <tr>
                            <th>Teacher Name</th>
                            <th>Subject</th>
                            <th>Duration (hours)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Teacher teacher : teachers) { %>
                            <tr>
                                <td><%= teacher.getName() %></td>
                                <td><%= teacher.getSubject().getName() %></td>
                                <td><%= teacher.getSubject().getDuration() %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } %>
            
            <h2>Configuration</h2>
            <ul>
                <li>Number of Classes: <%= classCount %></li>
                <li>School Hours: 9 AM to 4 PM (7 hours)</li>
                <li>Compulsory Break: 1 PM to 2 PM daily</li>
                <li>Classes per Day: 3-4</li>
                <li>Class Frequency:
                    <ul>
                        <li>JAVA, DSA, Math, OOSE: 3 classes per week</li>
                        <li>Linux, Backend: 2 classes per week</li>
                    </ul>
                </li>
                <li>Teacher Workload:
                    <ul>
                        <li>JAVA, DSA, Linux Teachers: 3 classes max</li>
                        <li>OOSE, Backend, Math Teachers: 6 classes max</li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
</body>
</html>
