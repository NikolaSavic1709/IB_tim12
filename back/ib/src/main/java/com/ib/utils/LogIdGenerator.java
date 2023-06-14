package com.ib.utils;

import com.ib.controller.AuthenticationController;
import org.slf4j.MDC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LogIdGenerator {
    public static void setLogId(){
        String logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
    }
    private static int getLastLogId() {
        String logFilePath = "src/main/resources/logs/application.log";
        String lastId = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lastId = extractLogId(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(lastId!=null)
            return Integer.parseInt(lastId);
        return 0;
    }
    private static String extractLogId(String logEntry) {
        int idStartIndex = logEntry.indexOf("[") + 1;
        int idEndIndex = logEntry.indexOf("]");
        return logEntry.substring(idStartIndex, idEndIndex);
    }
}
