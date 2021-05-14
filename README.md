# Nile Parent

[![Publish Snapshot](https://github.com/sourcesense/nile-parent/actions/workflows/master.yaml/badge.svg)](https://github.com/sourcesense/nile-parent/actions/workflows/master.yaml)


Nile Parent is a maven multi model project that has common moduls used by other nile projects, and has a parent pom gathering dependancies versions.

Nile parent is developed using Sping framework and Java 11.

Modules are the following:

| module              | description                                                             |
| ------------------- | ----------------------------------------------------------------------- |
| nile-common-core    | Common configuration and common springservices                          |
| nile-schema-engine  | Contains the service that do the actual transofrmation using the schema |
| nile-connector-core | Common configurations and services to develop custom connectors         |


## Documentation

Nile Usage documentation is available [here](https://sourcesense.github.io/nile/docs/import-gateway).


### Build

> Building Nile Parent requires JDK 11

```bash 
mvn clean install
```
