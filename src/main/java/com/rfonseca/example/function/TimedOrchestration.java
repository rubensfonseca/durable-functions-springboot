package com.rfonseca.example.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.microsoft.durabletask.azurefunctions.DurableClientContext;
import com.microsoft.durabletask.azurefunctions.DurableClientInput;

public class TimedOrchestration {

    @FunctionName("StartBatch")
    public void startBatch(
            @TimerTrigger(name = "startBatchTrigger", schedule = "5 * * * * *") String timerInfo,
            @DurableClientInput(name = "durableContext") DurableClientContext durableContext,
            ExecutionContext context) {
        // timeInfo is a JSON string, you can deserialize it to an object using your
        // favorite JSON library
        context.getLogger().info("Batch is triggered at: " + timerInfo);
    }
}
