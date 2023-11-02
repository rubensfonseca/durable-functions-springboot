package com.rfonseca.example.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

import java.util.Arrays;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import com.microsoft.azure.functions.ExecutionContext;

@ExtendWith(MockitoExtension.class)
class TestReportActivity {

    static class ExecutionContextMock implements ExecutionContext {

        @InjectMocks
        Logger logger;

        @Override
        public Logger getLogger() {
            return logger;
        }

        @Override
        public String getInvocationId() {
            return "invocation_id";
        }

        @Override
        public String getFunctionName() {
            return "function_name";
        }
    }

    @InjectMocks
    private ExecutionContextMock executionContext;

    @Mock
    private Logger logger;

    @BeforeEach
    void init() {

        doAnswer((Answer<Void>) invocation -> {
            Object[] args = invocation.getArguments();
            System.out.println("Called with arguments: " + Arrays.toString(args));
            return null;
        }).when(logger).info(anyString());
    }

    @Test
    @DisplayName("Test building daily report")
    void recoveryBusLogFiles() {

        ReportActivity activityFunction = new ReportActivity();
        assertEquals("report.csv", activityFunction.buildDailyReport("21/10/2023", executionContext));
    }

    @Test
    @DisplayName("Test sending mail report")
    void sendMailReport() {

        ReportActivity activityFunction = new ReportActivity();
        assertEquals("content", activityFunction.sendMailReport("report.csv", executionContext));
    }

}