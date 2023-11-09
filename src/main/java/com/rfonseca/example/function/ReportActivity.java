package com.rfonseca.example.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.durabletask.azurefunctions.DurableActivityTrigger;
import com.rfonseca.example.util.EcoBusUtil;

public class ReportActivity {

    @FunctionName("BuildDailyReport")
    public String buildDailyReport(
            @DurableActivityTrigger(name = "date") String date,
            final ExecutionContext context) {

        context.getLogger().info("Building report for " + date);

        Map<String, Integer> busLineCountMap = countBadgesPerBusLine(listLogEntries(date));

        saveDailyReport(date, busLineCountMap);

        return "report.csv";
    }

    private void saveDailyReport(String date, Map<String, Integer> busLineCountMap) {

        TableClient tableClient = getTableClient(EcoBusUtil.ECOBUS_REPORT_TABLE);

        // Print the counts for each BusLine
        for (Map.Entry<String, Integer> entry : busLineCountMap.entrySet()) {
            System.out.printf("BusLine %s has %d rows%n", entry.getKey(), entry.getValue());
            Map<String, Object> properties = new HashMap<>();

            properties.put("Date", date);
            properties.put("BusLineNumber", entry.getKey());
            properties.put("PassengersCount", entry.getValue());

            tableClient.upsertEntity(
                    new TableEntity(EcoBusUtil.ECOBUS_REPORT_TABLE, String.join("_", date, entry.getKey()))
                            .setProperties(properties));
        }

    }

    @FunctionName("SendMailReport")
    public String sendMailReport(
            @DurableActivityTrigger(name = "csv") String csvFile,
            final ExecutionContext context) {
        context.getLogger().info("Sending mail report with" + csvFile);
        return "content";
    }

    private Iterable<TableEntity> listLogEntries(String date) {
        List<String> propertiesToSelect = new ArrayList<>();
        propertiesToSelect.add("Date");
        propertiesToSelect.add("BusLineNumber");

        return listEntities(new ListEntitiesOptions()
                .setFilter(String.format("Date eq '%s'", date))
                .setSelect(propertiesToSelect));

    }

    public Iterable<TableEntity> listEntities(ListEntitiesOptions options) {
        TableClient tableClient = getTableClient(EcoBusUtil.ECOBUS_LOG_TABLE);

        return tableClient.listEntities(options, null, null);
    }

    private Map<String, Integer> countBadgesPerBusLine(Iterable<TableEntity> listLogEntries) {
        // Use a Map to store the count of rows for each BusLine
        Map<String, Integer> busLineCountMap = new HashMap<>();

        for (TableEntity entity : listLogEntries) {
            Map<String, Object> properties = entity.getProperties();

            // Assuming BusLineNumber is a String, adjust the type accordingly
            String busLineNumber = (String) properties.get("BusLineNumber");

            // Update the count for the current BusLine in the map
            busLineCountMap.put(busLineNumber, busLineCountMap.getOrDefault(busLineNumber, 0) + 1);

        }

        return busLineCountMap;

    }

    public TableClient getTableClient(String tableName) {
        return EcoBusUtil.getTableClient(tableName);
    }
}