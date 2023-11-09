package com.rfonseca.example.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.microsoft.azure.functions.ExecutionContext;
import com.rfonseca.example.util.EcoBusUtil;

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

        ReportActivity spyReportActivityFunction = spy(new ReportActivity());

        TableClient mockedTableClient = Mockito.mock(TableClient.class);

        doReturn(mockedTableClient).when(spyReportActivityFunction).getTableClient(EcoBusUtil.ECOBUS_LOG_TABLE);

        doReturn(mockedTableEntities()).when(spyReportActivityFunction).listEntities(any());

        // Use ArgumentCaptor to capture the argument passed to upsertEntity
        ArgumentCaptor<TableEntity> entityCaptor = ArgumentCaptor.forClass(TableEntity.class);

        // Use a list to store all captured entities
        List<TableEntity> capturedEntities = new ArrayList<>();

        doAnswer(invocation -> {
            TableEntity capturedEntity = entityCaptor.getValue();
            if (capturedEntity != null) {
                capturedEntities.add(capturedEntity);
            }
            return null;
        }).when(mockedTableClient).upsertEntity(entityCaptor.capture());

        assertEquals("report.csv", spyReportActivityFunction.buildDailyReport("21/10/2023", executionContext));
        assertEquals(2, capturedEntities.size());

    }

    private Iterable<TableEntity> mockedTableEntities() {
        Map<String, Object> properties1 = new HashMap<>();
        properties1.put("Date", "21/10/2023");
        properties1.put("Time", "10:10:00");
        properties1.put("BadgeNumber", "3NL9L7");
        properties1.put("ReaderCode", "46258");
        properties1.put("BusLineNumber", "Line 1");

        Map<String, Object> properties2 = new HashMap<>();
        properties2.put("Date", "21/10/2023");
        properties2.put("Time", "11:11:00");
        properties2.put("BadgeNumber", "PWB984");
        properties2.put("ReaderCode", "46258");
        properties2.put("BusLineNumber", "Line 1");

        Map<String, Object> properties3 = new HashMap<>();
        properties3.put("Date", "21/10/2023");
        properties3.put("Time", "10:10:00");
        properties3.put("BadgeNumber", "RQ2QNQ");
        properties3.put("ReaderCode", "38044");
        properties3.put("BusLineNumber", "Line 6");

        List<TableEntity> entities = Arrays.asList(
                new TableEntity("EcoBusRecords", "1697688047_3NL9L7_46258").setProperties(properties1),
                new TableEntity("EcoBusRecords", "1697688047_PWB984_46258").setProperties(properties2),
                new TableEntity("EcoBusRecords", "1697688047_3NL9L7_46258").setProperties(properties3));

        return entities;
    }

    @Test
    @DisplayName("Test sending mail report")
    void sendMailReport() {

        ReportActivity activityFunction = new ReportActivity();

        assertEquals("content", activityFunction.sendMailReport("report.csv", executionContext));
    }

}
