package com.ib.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.*;

import static com.ib.utils.LogIdGenerator.setLogId;

@Aspect
@Component
public class LogsRotationAspect {
    final int LOG_SIZE=30;
    @Pointcut("execution(* com.ib.controller..*(..))")
    public void controllerEndpointsPointcut() {
    }
    @Before(value = "controllerEndpointsPointcut()")
    public void logRepositoryMethodsBefore(JoinPoint joinPoint) {
        deleteLogsFromStart();
    }

    private void deleteLogsFromStart()
    {
        int linesToSkip=getDeleteLinesCount();
        if(linesToSkip<=0) return;
        String fileName = "src/main/resources/logs/application.log";
        String tempFileName = "src/main/resources/logs/temp.log";
        try {
            BufferedReader reader = getLogFileReader();
//            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFileName));
            StringBuilder buffer=new StringBuilder();
            String currentLine;
            int lineCount = 0;

            while ((currentLine = reader.readLine()) != null) {
                lineCount++;

                if (lineCount <= linesToSkip) {
                    continue;
                }

                buffer.append(currentLine);
                buffer.append("\n");
            }

            reader.close();
            //writer.close();
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(buffer.toString());
            writer.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int getDeleteLinesCount()
    {
        try (BufferedReader reader = getLogFileReader()) {
            int lineCount = 0;

            while (reader.readLine() != null) {
                lineCount++;
            }
            return lineCount-LOG_SIZE<=0 ? 0 : getCompleteRequestsSize(lineCount-LOG_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private int getCompleteRequestsSize(int min){
        String requestStartIndicator="Controller - Request";

        try (BufferedReader reader = getLogFileReader()) {
            int lineCount = 0, lastIndex=0;
            String line;
            while (lastIndex<min && (line=reader.readLine()) != null) {
                if(line.contains(requestStartIndicator))
                    lastIndex=lineCount;
                lineCount++;
            }
            return lastIndex;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private BufferedReader getLogFileReader() throws FileNotFoundException {
        String fileName = "src/main/resources/logs/application.log";
        return new BufferedReader(new FileReader(fileName));
    }
}


