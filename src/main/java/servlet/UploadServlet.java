package servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.Subject;
import model.Teacher;
import model.Timetable;
import util.CSVParser;
import util.TimetableGenerator;

@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get the uploaded file
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();

            // Parse the CSV file
            Map<String, Teacher> teachers = CSVParser.parseTeachers(fileContent);
            Map<String, Subject> subjects = CSVParser.parseSubjects();
            
            // Get the number of classes
            int classCount = 1;
            String classCountParam = request.getParameter("classCount");
            if (classCountParam != null && !classCountParam.isEmpty()) {
                try {
                    classCount = Integer.parseInt(classCountParam);
                } catch (NumberFormatException e) {
                    // Use default value
                }
            }
            
            // Convert maps to lists for the new TimetableGenerator
            List<Teacher> teacherList = new ArrayList<>(teachers.values());
            List<Subject> subjectList = new ArrayList<>(subjects.values());
            
            // Generate timetables
            TimetableGenerator generator = new TimetableGenerator();
            List<Timetable> timetables = generator.generateTimetables(teacherList, subjectList, classCount);
            
            // Store the timetables in the session
            request.getSession().setAttribute("timetables", timetables);
            request.getSession().setAttribute("classCount", classCount);
            
            // Redirect to the timetable.jsp page
            response.sendRedirect("timetable.jsp");
        } catch (IOException | ServletException e) {
            java.util.logging.Logger.getLogger(UploadServlet.class.getName()).log(java.util.logging.Level.SEVERE, "Error processing file", e);
            request.setAttribute("error", "Error processing file: " + e.getMessage());
            request.getRequestDispatcher("index.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            java.util.logging.Logger.getLogger(UploadServlet.class.getName()).log(java.util.logging.Level.WARNING, "Invalid class count format", e);
            request.setAttribute("error", "Invalid class count format: " + e.getMessage());
            request.getRequestDispatcher("index.jsp").forward(request, response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            java.util.logging.Logger.getLogger(UploadServlet.class.getName()).log(java.util.logging.Level.SEVERE, "Unexpected error", e);
            request.setAttribute("error", "Unexpected error: " + e.getMessage());
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }
}
