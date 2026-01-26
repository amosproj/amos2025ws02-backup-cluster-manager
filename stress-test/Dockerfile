FROM grafana/k6:latest

# Install curl for health checks and jq for JSON processing
USER root
RUN apk add --no-cache curl jq bash

WORKDIR /stress-test

# Copy test files
COPY backup-stress-test.js .
COPY run-stress-test.sh .

RUN chmod +x run-stress-test.sh && \
    mkdir -p results

ENTRYPOINT ["/bin/bash", "./run-stress-test.sh"]
