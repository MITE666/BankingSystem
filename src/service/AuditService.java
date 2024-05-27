package service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class AuditService {
    private static final String CSV_FILE_PATH = "logs/audit.csv";
    private static AuditService instance;

    private AuditService() {
        File file = new File(CSV_FILE_PATH);
        file.getParentFile().mkdirs();
    }

    public static AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public synchronized void logAction(String actionName) {
        LocalDateTime timestamp = LocalDateTime.now();
        String record = actionName + "," + timestamp;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH, true))) {
            writer.write(record);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


