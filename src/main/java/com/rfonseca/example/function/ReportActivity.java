package com.rfonseca.example.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.durabletask.azurefunctions.DurableActivityTrigger;

public class ReportActivity {

    @FunctionName("BuildDailyReport")
    public String buildDailyReport(
            @DurableActivityTrigger(name = "date") String date,
            final ExecutionContext context) {

        context.getLogger().info("Building report for " + date);

        return "report.csv";
    }

    @FunctionName("SendMailReport")
    public String sendMailReport(
            @DurableActivityTrigger(name = "csv") String csvFile,
            final ExecutionContext context) {
        context.getLogger().info("Sending mail report with" + csvFile);
        return "content";
    }
}
