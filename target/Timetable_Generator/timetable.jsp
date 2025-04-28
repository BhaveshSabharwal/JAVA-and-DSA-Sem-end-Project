<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="model.*" %>
<%@ page import="util.TimetableGenerator" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Generated Timetable</title>
    <link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
    <div class="container">
        <h1>Generated Timetable</h1>
        
        <% 
            // Get data from session
            ArrayList<Teacher> teachers = (ArrayList<Teacher>) session.getAttribute("teachers");
            ArrayList<Subject> subjects = (ArrayList<Subject>) session.getAttribute("subjects");
            int classCount = (Integer) session.getAttribute("classCount");
            
            // Check if we have the necessary data
            if (teachers == null || teachers.isEmpty() || subjects == null || subjects.isEmpty()) {
                response.sendRedirect("index.jsp");
                return;
            }
            
            // Generate timetables if not already generated
            if (session.getAttribute("timetables") == null) {
                TimetableGenerator generator = new TimetableGenerator();
                List<Timetable> timetables = generator.generateTimetables(teachers, subjects, classCount);
                session.setAttribute("timetables", timetables);
            }
            
            List<Timetable> timetables = (List<Timetable>) session.getAttribute("timetables");
            
            // Handle class selection
            int selectedClass = 0;
            String classParam = request.getParameter("class");
            if (classParam != null && !classParam.trim().isEmpty()) {
                try {
                    selectedClass = Integer.parseInt(classParam);
                    if (selectedClass < 0 || selectedClass >= classCount) {
                        selectedClass = 0;
                    }
                } catch (NumberFormatException e) {
                    // Invalid input, use default
                }
            }
        %>
        
        <div class="class-selector">
            <h2>Select Class</h2>
            <form method="get" action="timetable.jsp">
                <select name="class" onchange="this.form.submit()">
                    <% for (int i = 0; i < classCount; i++) { %>
                        <option value="<%= i %>" <%= (i == selectedClass) ? "selected" : "" %>>Class <%= i + 1 %></option>
                    <% } %>
                </select>
            </form>
            <a href="index.jsp" class="btn back-btn">Back to Input</a>
        </div>
        
        <% if (timetables != null && !timetables.isEmpty() && selectedClass < timetables.size()) { %>
            <div class="timetable-container">
                <h2>Timetable for Class <%= selectedClass + 1 %></h2>
                <table class="timetable">
                    <thead>
                        <tr>
                            <th>Time</th>
                            <th>Monday</th>
                            <th>Tuesday</th>
                            <th>Wednesday</th>
                            <th>Thursday</th>
                            <th>Friday</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                            Timetable timetable = timetables.get(selectedClass);
                            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
                            String[] timeSlots = {"9:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-1:00", "1:00-2:00", "2:00-3:00", "3:00-4:00"};
                            
                            for (int timeSlot = 0; timeSlot < timeSlots.length; timeSlot++) {
                        %>
                            <tr>
                                <td><%= timeSlots[timeSlot] %></td>
                                <% 
                                    // Special handling for lunch break
                                    if (timeSlot == 4) { // 1:00-2:00 slot
                                %>
                                    <td colspan="5" class="lunch-break">LUNCH BREAK</td>
                                <%
                                    } else {
                                        for (int day = 0; day < days.length; day++) {
                                            TimetableEntry entry = timetable.getEntryByDayAndTime(day, timeSlot);
                                            if (entry != null) {
                                                String rowspan = "";
                                                if (entry.getSubject().getDuration() > 1 && timeSlot == entry.getTimeSlot().getStartTime()) {
                                                    rowspan = "rowspan=\"" + entry.getSubject().getDuration() + "\"";
                                                }
                                                
                                                if (rowspan.isEmpty() || timeSlot == entry.getTimeSlot().getStartTime()) {
                                %>
                                    <td <%= rowspan %> class="subject-cell">
                                        <div class="subject"><%= entry.getSubject().getName() %></div>
                                        <div class="teacher"><%= entry.getTeacher().getName() %></div>
                                    </td>
                                <%
                                                }
                                            } else {
                                                // Check if this slot is part of a multi-hour class
                                                boolean isPartOfMultiHour = false;
                                                for (int prevSlot = 0; prevSlot < timeSlot; prevSlot++) {
                                                    TimetableEntry prevEntry = timetable.getEntryByDayAndTime(day, prevSlot);
                                                    if (prevEntry != null && 
                                                        prevEntry.getSubject().getDuration() > 1 && 
                                                        prevSlot + prevEntry.getSubject().getDuration() > timeSlot) {
                                                        isPartOfMultiHour = true;
                                                        break;
                                                    }
                                                }
                                                
                                                if (!isPartOfMultiHour) {
                                %>
                                    <td class="empty-cell">-</td>
                                <%
                                                }
                                            }
                                        }
                                    }
                                %>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } else { %>
            <div class="error-message">
                <p>No timetable data available. Please go back and generate timetables.</p>
            </div>
        <% } %>
    </div>
</body>
</html>
