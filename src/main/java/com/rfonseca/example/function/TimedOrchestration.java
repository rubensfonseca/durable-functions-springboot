package com.rfonseca.example.function;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.microsoft.durabletask.DurableTaskClient;
import com.microsoft.durabletask.RetryPolicy;
import com.microsoft.durabletask.Task;
import com.microsoft.durabletask.TaskOptions;
import com.microsoft.durabletask.TaskOrchestrationContext;
import com.microsoft.durabletask.azurefunctions.DurableClientContext;
import com.microsoft.durabletask.azurefunctions.DurableClientInput;
import com.microsoft.durabletask.azurefunctions.DurableOrchestrationTrigger;

@Component
public class TimedOrchestration {

        @FunctionName("StartBatch")
        public void startBatch(
                        @TimerTrigger(name = "startBatchTrigger", schedule = "0 0 21 * * *") String timerInfo,
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

                String currentDate = "2023-10-19";

                // Get the list of files transfered from the bus
                List<?> busLogFileList = ctx.callActivity("RecoverBusLogFiles",
                                currentDate, List.class).await();

                List<Task<Integer>> logProcessTasks = busLogFileList.stream()
                                .map(item -> ctx.callActivity("ProcessBusLogFile", item.toString(),
                                                Integer.class))
                                .collect(Collectors.toList());

                // Process logs in parallel
                List<Integer> results = ctx.allOf(logProcessTasks).await();

                context.getLogger().info("Log files processed " + results.size());
                context.getLogger().info(
                                "Ammount of lines processed " + results.stream().mapToInt(Integer::intValue).sum());

                // Build daily report as csv file for a given date
                ctx.callActivity("BuildDailyReport", currentDate,
                                String.class).await();

                
                RetryPolicy retryPolicy = new RetryPolicy(5, Duration.ofSeconds(60));
                TaskOptions taskOptions = new TaskOptions(retryPolicy);

                // Send report by mail
                ctx.callActivity("SendMailReport", currentDate, taskOptions).await();

        }

}
