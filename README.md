# Nile Mongodb Rest

[![Publish Snapshot](https://github.com/sourcesense/nile-mongodb-rest/actions/workflows/master.yaml/badge.svg)](https://github.com/sourcesense/nile-mongodb-rest/actions/workflows/master.yaml)

Is the REST api from which you can consume the final output of Nile transofrmations.

```
http://${INTERNAL_URL}:${port}/docs/index.html#/default
```
## Documentation

The full documentation is available [here](https://sourcesense.github.io/nile/docs/mongodb-rest).

## Install

Nile Mongodb Rest is distributed as a Docker container:

```bash
docker pull sourcesense/nile-mongodb-rest:latest
```

Refer to this [docker-compose](https://github.com/sourcesense/nile-compose/blob/master/docker-compose.yaml) to know how to configure it.

## Developing

```bash
npm install
npm run dev
```
