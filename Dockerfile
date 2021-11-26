FROM       node:14-slim

RUN apt-get update || : && apt-get install python -y


WORKDIR    /usr/joyce

# Copy and install production packages
COPY       src src/
COPY       package*.json ./
COPY       tsconfig.json ./

# COPY       .meshrc.yml ./
COPY       mesh-server.js ./
COPY       assets/schemas.json /usr/joyce/schemas.json
COPY       static /usr/joyce/static
RUN        mkdir -p /usr/joyce/assets
RUN        mkdir -p /usr/joyce/.mesh
RUN        chown -R node:node /usr/joyce

RUN        npm ci && npm run build

USER       node
ENV        NODE_ENV="production"
ENV        SCHEMAS_SOURCE=/usr/joyce/schemas.json
EXPOSE     6650
RUN touch .meshrc.yml
# Running port is configured through API_PORT env variable
ENTRYPOINT ["npm"]
CMD        ["run", "mesh"]
