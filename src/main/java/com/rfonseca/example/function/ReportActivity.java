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
    public void buildDailyReport(
            @DurableActivityTrigger(name = "date") String date,
            final ExecutionContext context) {

        context.getLogger().info("Building report for " + date);

        Map<String, Integer> busLineCountMap = countBadgesPerBusLine(listLogEntries(date));

        saveDailyReport(date, busLineCountMap);

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
            @DurableActivityTrigger(name = "date") String date,
            final ExecutionContext context) {
        context.getLogger().info("Sending mail report with" + date);

        List<String> propertiesToSelect = new ArrayList<>();
        propertiesToSelect.add("BusLineNumber");
        propertiesToSelect.add("PassengersCount");

        Iterable<TableEntity> reportEntities = listReportEntities(new ListEntitiesOptions()
                .setFilter(String.format("Date eq '%s'", date))
                .setSelect(propertiesToSelect));

        // Create the mail content using StringBuilder
        StringBuilder mailContent = new StringBuilder();
        mailContent.append("Date: ").append(date).append("\n\n");
        mailContent.append("<table border=\"1\">");
        mailContent.append("<tr><th>Bus Line Number</th><th>Passengers Count</th></tr>");

        for (TableEntity entity : reportEntities) {
            mailContent.append("<tr>");
            mailContent.append("<td>").append(entity.getProperty("BusLineNumber").toString()).append("</td>");
            mailContent.append("<td>").append(entity.getProperty("PassengersCount").toString()).append("</td>");
            mailContent.append("</tr>");
        }

        mailContent.append("</table>");

        return mailContent.toString();
    }

    private Iterable<TableEntity> listLogEntries(String date) {
        List<String> propertiesToSelect = new ArrayList<>();
        propertiesToSelect.add("Date");
        propertiesToSelect.add("BusLineNumber");

        return listLogsEntities(new ListEntitiesOptions()
                .setFilter(String.format("Date eq '%s'", date))
                .setSelect(propertiesToSelect));

    }

    public Iterable<TableEntity> listReportEntities(ListEntitiesOptions options) {
        TableClient tableClient = getTableClient(EcoBusUtil.ECOBUS_REPORT_TABLE);

        return tableClient.listEntities(options, null, null);
    }

    public Iterable<TableEntity> listLogsEntities(ListEntitiesOptions options) {
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