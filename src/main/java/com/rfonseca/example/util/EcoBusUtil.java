package com.rfonseca.example.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;

public class EcoBusUtil {

    private static final String ECO_BUS_STORAGE_CONNECTION_STRING = System.getenv("EcoBusStorageConnectionString");
    public static final String ECOBUS_LOG_TABLE = "EcoBusRecords";
    public static final String ECOBUS_REPORT_TABLE = "EcoBusReports";

    public static String formatDateUnixTimestamp(long unixTimestamp) {
        // Create a SimpleDateFormat with the desired date-time format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Convert Unix timestamp to Date
        Date date = new Date(unixTimestamp * 1000); // Multiply by 1000 to convert
        // seconds to milliseconds

        // Format the Date to a string
        return dateFormat.format(date);

    }

    public static String formatTimeUnixTimestamp(long unixTimestamp) {
        // Create a SimpleDateFormat with the desired date-time format
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        // Convert Unix timestamp to Date
        Date date = new Date(unixTimestamp * 1000); // Multiply by 1000 to convert
        // seconds to milliseconds

        // Format the Date to a string
        return dateFormat.format(date);

    }

    public static TableClient getTableClient(String tableName) {
        return new TableClientBuilder()
                .connectionString(ECO_BUS_STORAGE_CONNECTION_STRING) // or use any of the other
                // authentication methods
                .tableName(tableName)
                .buildClient();
    }
}
