FROM       node:14-alpine

WORKDIR    /usr/nile

# Copy and install production packages
COPY       src src/
COPY       package*.json ./
COPY       tsconfig.json ./
RUN        npm ci && npm run build

# Non root user
USER       node
ENV        NODE_ENV="production"
EXPOSE     6650

# Running port is configured through API_PORT env variable
ENTRYPOINT ["node"]
CMD        ["dist/index.js"]
