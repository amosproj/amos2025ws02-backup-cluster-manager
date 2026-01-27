FROM grafana/k6:latest

# Install curl for health checks and jq for JSON processing
USER root
RUN apk add --no-cache curl jq bash dos2unix


WORKDIR /stress-test

# Copy test files
COPY backup-stress-test.js .
COPY run-stress-test.sh .

# convert first, then execute
RUN dos2unix run-stress-test.sh && \
    chmod +x run-stress-test.sh && \
    mkdir -p results

ENTRYPOINT ["/bin/bash", "./run-stress-test.sh"]