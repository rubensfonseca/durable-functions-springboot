# Use Case: Implementing a Business Workflow for EcoBus with Azure Durable Functions and Java SpringBoot

This use case involves leveraging Azure Durable Functions and a Java SpringBoot Application to orchestrate a simple business workflow for EcoBus, a fictional company managing a fleet of buses.

## Workflow Overview

Upon passengers entering the bus, each must badge in using a badge reader for payment. The recorded data is stored in a local file, maintaining a daily history of badges.

Every night, as the buses return to the garage, the badge logs are downloaded to an Azure Storage account. Subsequently, a batch process is triggered at 21:00 to parse the logs, consolidate the data, and generate a report.

### Workflow Steps

1. Passengers badge in upon entering the bus.
2. Badge data is recorded in a local file, maintaining a daily log.
3. Nightly, badge logs are transferred to an Azure Storage account.
4. A batch process is triggered at 21:00 to:
   - Parse badge logs.
   - Consolidate data.
   - Generate and send a detailed report via email.

This implementation streamlines EcoBus operations, ensuring efficient management of passenger entries, secure data storage, and automated reporting for improved business insights.

## Run in vscode

* Install and run Azurite extension to start a storage account emulator locally.

* Connect to the local storage account emulator with Storage Explorer, create a blob container named `bus-logs`, and upload the files from the `data/busLogFile` folder.

* Package the compile, test, and package the functions with `mvn package`.

* Run the functions locally with `mvn azure-functions:run`.

* With `curl` or a REST API Tool, perform a GET to the following endpoint to retrieve all available functions:
  http://localhost:7071/admin/functions/

* With `curl` or a REST API Tool, perform a POST to the following endpoint to trigger the workflow:
  http://localhost:7071/admin/functions/StartBatch

## Note

*Disclaimer: The code provided is intended for learning and demonstration purposes without any warranty.*

This example is provided under the GNU license. For any suggestions or contributions, you can reach me at rubens.fonseca@gmail.com.

## References

Microsoft Learn: https://learn.microsoft.com/en-us/azure/azure-functions/durable/