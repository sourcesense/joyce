# Joyce Parent

[![Publish Snapshot](https://github.com/sourcesense/nile-parent/actions/workflows/master.yaml/badge.svg)](https://github.com/sourcesense/nile-parent/actions/workflows/master.yaml)


Joyce Parent is a maven multi model project that has common moduls used by other Joyce projects, and has a parent pom gathering dependancies versions.

Joyce parent is developed using Sping framework and Java 11.

Modules are the following:

| module              | description                                                             |
| ------------------- | ----------------------------------------------------------------------- |
| joyce-common-core    | Common configuration and common springservices                          |
| joyce-schema-engine  | Contains the service that do the actual transofrmation using the schema |
| joyce-connector-core | Common configurations and services to develop custom connectors         |


## Documentation

Joyce Usage documentation is available [here](https://sourcesense.github.io/joyce/docs/import-gateway).


### Build

> Building Joyce Parent requires JDK 11

```bash 
mvn clean install
```
