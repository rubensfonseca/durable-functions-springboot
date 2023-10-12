package com.rfonseca.example.function;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.durabletask.azurefunctions.DurableActivityTrigger;

public class BusLogFileActivity {

    @FunctionName("RecoveryBusLogFiles")
    public List<String> recoveryBusLogFiles(
            @DurableActivityTrigger(name = "date") String date,
            final ExecutionContext context) {
        context.getLogger().info("Retrieving files for " + date);

        return Arrays.asList("file1", "file2", "file3");
    }

    @FunctionName("ProcessBusLogFile")
    public Integer processBusLogFile(
            @DurableActivityTrigger(name = "fileName") String fileName,
            final ExecutionContext context) {
        context.getLogger().info("Processing file" + fileName);

        return new Random().nextInt(30, 100);
    }
}
