package com.rfonseca.example.function;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.microsoft.durabletask.DurableTaskClient;
import com.microsoft.durabletask.Task;
import com.microsoft.durabletask.TaskOrchestrationContext;
import com.microsoft.durabletask.azurefunctions.DurableClientContext;
import com.microsoft.durabletask.azurefunctions.DurableClientInput;
import com.microsoft.durabletask.azurefunctions.DurableOrchestrationTrigger;

@Component
public class TimedOrchestration {

    @FunctionName("StartBatch")
    public void startBatch(
            @TimerTrigger(name = "startBatchTrigger", schedule = "5 * * * * *") String timerInfo,
            @DurableClientInput(name = "durableContext") DurableClientContext durableContext,
            ExecutionContext context) {
        // can replace schedule with a variable %CRON_EXPRESSION%
        // timeInfo is a JSON string, you can deserialize it to an object using your
        // favorite JSON library
        context.getLogger().info("Batch is triggered at: " + timerInfo);

        DurableTaskClient client = durableContext.getClient();
        String instanceId = client.scheduleNewOrchestrationInstance("DailyBatchOrchestration");

        context.getLogger().info("Scheduled new orchestration with ID " + instanceId);

    }

    @FunctionName("DailyBatchOrchestration")
    public void dailyBatchOrchestration(
            @DurableOrchestrationTrigger(name = "ctx") TaskOrchestrationContext ctx,
            ExecutionContext context) {

        context.getLogger().info("Running daily batch orchestration " + ctx.getInstanceId());

        // RetryPolicy retryPolicy = new RetryPolicy(2, Duration.ofSeconds(1));
        // TaskOptions taskOptions = new TaskOptions(retryPolicy);


        // Get the list of files transfered from the bus
        List<?> busLogFileList = ctx.callActivity("RecoveryBusLogFiles",
        "25/10/2020", List.class).await();

        List<Task<Integer>> logProcessTasks = busLogFileList.stream()
        .map(item -> ctx.callActivity("ProcessBusLogFile", item.toString(),
        Integer.class))
        .collect(Collectors.toList());

        // //Process logs in parallel
        List<Integer> results = ctx.allOf(logProcessTasks).await();

        context.getLogger().info("Log files processed " + results.size());

        //Build daily report as csv file for a given date
        String reportCsvUri = ctx.callActivity("BuildDailyReport", "",
        String.class).await();

        //Send report by mail
        ctx.callActivity("SendMailReport", reportCsvUri).await();

    }




}
