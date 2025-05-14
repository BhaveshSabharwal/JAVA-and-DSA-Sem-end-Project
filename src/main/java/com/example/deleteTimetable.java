package com.example;

import java.io.File;

public class deleteTimetable {
    public boolean deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("✅ File deleted successfully!");
                return true;
            } else {
                System.out.println("❌ Failed to delete the file.");
                return false;
            }
        } else {
            System.out.println("❌ File not found.");
            return false;
        }
    }
}