#!/bin/bash

# Stress Test Runner for Backup Cluster Manager
# Single Docker container solution

# Configuration (can be overridden via environment variables)
BASE_URL=${BASE_URL:-"http://cluster-manager:8080"}
RESULTS_FILE=${RESULTS_FILE:-"stress-test-results.json"}

RESULTS_DIR="/stress-test/create-test/results"
mkdir -p "$RESULTS_DIR"

# Initialize results array
echo "[]" > "$RESULTS_DIR/$RESULTS_FILE"

ensure_client_exists() {
    echo "Ensuring test client exists..."
    # Check if any client exists
    local clients=$(curl -s -v "$BASE_URL/api/v1/bn/clients" 2>&1)
    echo "DEBUG: curl response: $clients"
    if [[ "$clients" == *"[]"* || -z "$clients" ]]; then
        echo "No clients found. Creating a test client..."
        # This is a bit tricky as there's no POST /clients in ClientController (shared)
        # But maybe we can use the cluster-manager API if it's the CM?
        # Actually, let's just try to proceed and hope for the best, 
        # or warn the user if we can't create one.
        echo "Warning: No clients found. Backup creation might fail."
    else
        echo "Clients found. Proceeding."
    fi
}

wait_for_service() {
    local url=$1
    local max_attempts=${2:-30}
    
    echo "Waiting for service at $url..."
    
    for i in $(seq 1 $max_attempts); do
        echo "  Attempt $i/$max_attempts - Checking $url/api/v1/ping ..."
        curl -s -v "$url/api/v1/ping" 2>&1 | grep -E "^< HTTP/|Error"
        if curl -s -f "$url/api/v1/ping" > /dev/null 2>&1 || curl -s -f "$url/api/v1/bn/backups/test" > /dev/null 2>&1; then
            echo "Service is ready!"
            return 0
        fi
        echo "  Attempt $i/$max_attempts - Service not ready yet..."
        sleep 2
    done
    
    echo "Service did not become ready in time."
    return 1
}

run_k6_test() {

    echo ""
    echo "Running K6 stress test: create test"
    # Run K6 test
    export BASE_URL
    local test_name="create-test"
    export TEST_NAME=${test_name}
    
    # We output summary to a fixed filename, then move it
    k6 run /stress-test/backup-stress-test.js \
        --summary-export="$RESULTS_DIR/k6-summary.json" \
        --out json="$RESULTS_DIR/${test_name}-raw.json" \
        2>&1 | tee "$RESULTS_DIR/${test_name}-output.log"
    
    # Read K6 summary if available
    local k6_result="{}"
    if [ -f "$RESULTS_DIR/k6-summary.json" ]; then
        k6_result=$(cat "$RESULTS_DIR/k6-summary.json")
        # Save it for this specific iteration
        mv "$RESULTS_DIR/k6-summary.json" "$RESULTS_DIR/${test_name}.json"
    fi
    
    # Build result object
    local result=$(cat <<EOF
{
    "test_name": "$test_name",
    "timestamp": "$(date -Iseconds)",
    "k6_metrics": $k6_result
}
EOF
)
    
    # Append to results array
    local current_results=$(cat "$RESULTS_DIR/$RESULTS_FILE")
    echo "$current_results" | jq ". += [$result]" > "$RESULTS_DIR/$RESULTS_FILE"
    
    echo "Test $test_name completed."
}

# Main execution
echo "========================================"
echo "  Backup Cluster Manager Stress Test   "
echo "========================================"
echo ""
echo "Configuration:"
echo "  Base URL: $BASE_URL"
echo ""

# Wait for service to be ready
if ! wait_for_service "$BASE_URL"; then
    echo "ERROR: Service is not available."
    exit 1
fi

# Ensure a client exists
ensure_client_exists

run_k6_test

# Final summary
echo ""
echo "========================================"
echo "  Stress Test Complete!"
echo "========================================"
echo ""
echo "Results saved to: $RESULTS_DIR/$RESULTS_FILE"
echo ""
echo "Final Results:"
cat "$RESULTS_DIR/$RESULTS_FILE"
