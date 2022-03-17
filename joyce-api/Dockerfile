FROM       node:14-slim

RUN apt-get update && apt-get install make

WORKDIR    /usr/joyce

COPY       package*.json ./
COPY       tsconfig.json ./
RUN        npm ci

COPY       static /usr/joyce/static
COPY       src src/
COPY       generated generated/

ENV        NODE_ENV="production"
EXPOSE     6650

# Running port is configured through API_PORT env variable
ENTRYPOINT ["npm"]
CMD        ["run", "start"]
