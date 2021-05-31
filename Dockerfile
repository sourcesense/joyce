FROM       node:14-alpine

WORKDIR    /usr/joyce

# Copy and install production packages
COPY       src src/
COPY       package*.json ./
COPY       tsconfig.json ./
COPY       assets/schemas.json /usr/joyce/schemas.json
RUN        npm ci && npm run build

# Non root user
USER       node
ENV        NODE_ENV="production"
ENV        SCHEMAS_SOURCE=/usr/joyce/schemas.json
EXPOSE     6650

# Running port is configured through API_PORT env variable
ENTRYPOINT ["node"]
CMD        ["dist/index.js"]
