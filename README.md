# durable-functions-springboot
An use case of implementing Azure Durable Functions with Java Spring Boot involves orchestration of a workflow.

## Sample
https://github.com/microsoft/durabletask-java

## Run in vscode

* Install Http client extension

* GET endpoint to retrieve all available functions
http://localhost:7071/admin/functions/

* POST endpoint to trigged timer function (empty body)
http://localhost:7071/admin/functions/StartBatch

MS doc: https://learn.microsoft.com/en-us/azure/azure-functions/functions-manually-run-non-http