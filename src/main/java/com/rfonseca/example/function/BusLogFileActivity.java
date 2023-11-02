package com.rfonseca.example.function;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

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

public class BusLogFileActivity {

    @Value("${EcoBusStorageConnectionString}")
    private String ecoBusStorageConnectionString;

    @Value("${EcoBusStorageContainer}")
    private String ecoBusStorageContainer;

    private CloudBlobContainer getBlobContainer() throws InvalidKeyException, URISyntaxException, StorageException {

        System.out.println(
                "Env Storage Account is " + System.getenv("EcoBusStorageConnectionString")
                        + " and container is " + System.getenv("EcoBusStorageContainer"));

        CloudStorageAccount storageAccount = CloudStorageAccount.parse(System.getenv("EcoBusStorageConnectionString"));
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        return blobClient.getContainerReference(System.getenv("EcoBusStorageContainer"));
    }

    public Iterable<ListBlobItem> listBlobs() throws InvalidKeyException, URISyntaxException, StorageException {
        return getBlobContainer().listBlobs();
    }

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

        Integer lineCounter = 0;

        try (BufferedReader reader = getReaderFileName(fileName)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCounter++;
            }
        }

        return lineCounter;
    }

    public BufferedReader getReaderFileName(String fileName)
            throws InvalidKeyException, URISyntaxException, StorageException, IOException {
        CloudBlockBlob blob = getBlobContainer().getBlockBlobReference(fileName);

        return new BufferedReader(
                new InputStreamReader(blob.openInputStream(), StandardCharsets.UTF_8));
    }

}
