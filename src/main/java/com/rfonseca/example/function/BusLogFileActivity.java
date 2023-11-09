package com.rfonseca.example.function;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.durabletask.azurefunctions.DurableActivityTrigger;
import com.rfonseca.example.util.EcoBusUtil;

public class BusLogFileActivity {

    // Static Map with default values initialized at the point of declaration
    private static final Map<String, String> readerLineMap = new HashMap<>();

    // You can also initialize the static Map in a static block
    static {
        readerLineMap.put("13935", "Line 1");
        readerLineMap.put("18122", "Line 6");
        readerLineMap.put("46258", "Line 10");
    }

    private String ecoBusStorageContainer = System.getenv("EcoBusStorageContainer");

    @FunctionName("RecoveryBusLogFiles")
    public List<String> recoveryBusLogFiles(
            @DurableActivityTrigger(name = "date") String date,
            final ExecutionContext context) throws URISyntaxException, InvalidKeyException, StorageException {

        context.getLogger().info("Retrieving files for " + date);

        List<String> outputList = new ArrayList<>();

        // List blobs in the container
        Iterable<ListBlobItem> blobs = listBlobs();

        System.out.println("Blobs from " + blobs);

        for (ListBlobItem blobItem : blobs) {
            if (blobItem instanceof CloudBlob) {
                CloudBlob blob = (CloudBlob) blobItem;
                context.getLogger().fine("Blob Name: " + blob.getName());
                outputList.add(blob.getName());
            }
        }
        return outputList;
    }

    @FunctionName("ProcessBusLogFile")
    public Integer processBusLogFile(
            @DurableActivityTrigger(name = "fileName") String fileName,
            final ExecutionContext context)
            throws InvalidKeyException, URISyntaxException, StorageException, IOException {
        context.getLogger().info("Processing file " + fileName);

        TableClient tableClient = getTableClient();

        Integer lineCounter = 0;

        try (BufferedReader reader = getReaderFileName(fileName)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCounter++;

                // Add the new customer to the Employees table.
                tableClient.upsertEntity(buildTableEntity(line));
                // save content in a table using
                // https://learn.microsoft.com/en-us/java/api/overview/azure/data-tables-readme?view=azure-java-stable
            }
        }

        return lineCounter;
    }

    private TableEntity buildTableEntity(String line) {
        // Spliting row data by space
        String[] fields = line.split("\\s+");

        if (fields.length < 3) {
            throw new IllegalArgumentException("Insufficient data to build TableEntity");
        }

        // Create a new table entity.
        Map<String, Object> properties = new HashMap<>();

        properties.put("Date", EcoBusUtil.formatDateUnixTimestamp(Long.parseLong(fields[0])));
        properties.put("Time", EcoBusUtil.formatTimeUnixTimestamp(Long.parseLong(fields[0])));
        properties.put("BadgeNumber", fields[1]);
        properties.put("ReaderCode", fields[2]);
        properties.put("BusLineNumber", readerLineMap.get(fields[2]));

        return new TableEntity(EcoBusUtil.ECOBUS_LOG_TABLE, line.replace(" ", "_"))
                .setProperties(properties);

    }

    private CloudBlobContainer getBlobContainer() throws InvalidKeyException, URISyntaxException, StorageException {

        CloudStorageAccount storageAccount = CloudStorageAccount.parse(System.getenv("EcoBusStorageConnectionString"));
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        return blobClient.getContainerReference(ecoBusStorageContainer);
    }

    public Iterable<ListBlobItem> listBlobs() throws InvalidKeyException, URISyntaxException, StorageException {
        return getBlobContainer().listBlobs();
    }

    public BufferedReader getReaderFileName(String fileName)
            throws InvalidKeyException, URISyntaxException, StorageException, IOException {
        CloudBlockBlob blob = getBlobContainer().getBlockBlobReference(fileName);

        return new BufferedReader(
                new InputStreamReader(blob.openInputStream(), StandardCharsets.UTF_8));
    }

    public TableClient getTableClient() {
        return EcoBusUtil.getTableClient(EcoBusUtil.ECOBUS_LOG_TABLE);
    }
}
